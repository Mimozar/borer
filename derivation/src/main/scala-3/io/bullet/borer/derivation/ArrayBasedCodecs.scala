/*
 * Copyright (c) 2019-2021 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.derivation

import io.bullet.borer.{Codec, Decoder, Encoder, Writer}
import scala.quoted.{Quotes, Expr, Type}
import scala.deriving._
import scala.compiletime._

/**
 * Derivation macros for array-based encodings.
 */
object ArrayBasedCodecs {

  /**
   * Macro that creates an [[Encoder]] for [[T]] provided that
   * - [[T]] is a `case class`, `sealed abstract class` or `sealed trait`
   * - [[Encoder]] instances for all members of [[T]] (if [[T]] is a `case class`)
   *   or all sub-types of [[T]] (if [[T]] is an ADT) are implicitly available
   *
   * Case classes are converted into an array of values, one value for each member.
   *
   * NOTE: If `T` is unary (i.e. only has a single member) then the member value is written in an unwrapped form,
   * i.e. without the array container.
   */
  inline def deriveEncoder[T :Mirror.Of]: Encoder[T] = ArrayBasedCodecMacros.EncoderDeriver.derive[T]

  /**
   * Macro that creates an [[Encoder]] for [[T]] and all direct and indirect sub-types of [[T]],
   * which are concrete, i.e. not abstract.
   * [[T]] must be a `sealed abstract class` or `sealed trait`.
   *
   * It works by generating a code block such as this one:
   *
   * {{{
   *   implicit val a = deriveEncoder[A]     // one such line is generated for each concrete
   *   implicit val b = deriveEncoder[B]     // direct or indirect sub-type of T which doesn't
   *   implicit val c = deriveEncoder[C]     // already have an implicit Encoder available
   *   ...
   *   deriveEncoder[T]
   * }}}
   *
   * If an [[Encoder]] for a certain concrete sub-type `S <: T` is already implicitly available
   * at the macro call-site the respective line for the sub-type is **not** generated.
   *
   * If an [[Encoder]] for a certain abstract sub-type `S <: T` is already implicitly available
   * at the macro call-site the respective lines for **all** sub-types of `S` are **not** generated.
   *
   * This means that you can specify your own custom Encoders for concrete sub-types or whole branches
   * of the sub-type hierarchy and they will be properly picked up rather than create conflicts.
   */
  inline def deriveAllEncoders[T]: Encoder[T] = ???

  /**
   * Macro that creates a [[Decoder]] for [[T]] provided that
   * - [[T]] is a `case class`, `sealed abstract class` or `sealed trait`
   * - [[Decoder]] instances for all members of [[T]] (if [[T]] is a `case class`)
   *   or all sub-types of [[T]] (if [[T]] is an ADT) are implicitly available
   *
   * Case classes are created from an array of deserialized values, one value for each member.
   *
   * NOTE: If `T` is unary (i.e. only has a single member) then the member value is expected in an unwrapped form,
   * i.e. without the array container.
   */
  inline def deriveDecoder[T]: Decoder[T] = ???

  /**
   * Macro that creates a [[Decoder]] for [[T]] and all direct and indirect sub-types of [[T]],
   * which are concrete, i.e. not abstract.
   * [[T]] must be a `sealed abstract class` or `sealed trait`.
   *
   * It works by generating a code block such as this one:
   *
   * {{{
   *   implicit val a = deriveDecoder[A]     // one such line is generated for each concrete
   *   implicit val b = deriveDecoder[B]     // direct or indirect sub-type of T which doesn't
   *   implicit val c = deriveDecoder[C]     // already have an implicit Decoder available
   *   ...
   *   deriveDecoder[T]
   * }}}
   *
   * If a [[Decoder]] for a certain concrete sub-type `S <: T` is already implicitly available
   * at the macro call-site the respective line for the sub-type is **not** generated.
   *
   * If a [[Decoder]] for a certain abstract sub-type `S <: T` is already implicitly available
   * at the macro call-site the respective lines for **all** sub-types of `S` are **not** generated.
   *
   * This means that you can specify your own custom Decoders for concrete sub-types or whole branches
   * of the sub-type hierarchy and they will be properly picked up rather than create conflicts.
   */
  inline def deriveAllDecoders[T]: Decoder[T] = ???

  /**
   * Macro that creates an [[Encoder]] and [[Decoder]] pair for [[T]].
   * Convenience shortcut for `Codec(deriveEncoder[T], deriveDecoder[T])"`.
   */
  inline def deriveCodec[T]: Codec[T] = ???

  /**
   * Macro that creates an [[Encoder]] and [[Decoder]] pair for [[T]] and all direct and indirect sub-types of [[T]].
   * Convenience shortcut for `Codec(deriveAllEncoders[T], deriveAllDecoders[T])"`.
   */
  inline def deriveAllCodecs[T]: Codec[T] = ???
}

object ArrayBasedCodecMacros {

  abstract class Deriver {
    inline def deriveSingleton[T](m: Mirror.Singleton): Encoder[T]
    inline def deriveProduct[T](m: Mirror.ProductOf[T]): Encoder[T]
    inline def deriveSum[T](m: Mirror.SumOf[T]): Encoder[T]

    inline def derive[T](using m: Mirror.Of[T]): Encoder[T] =
      inline m match {
        case x: Mirror.Singleton    => deriveSingleton[T](x)
        case x: Mirror.ProductOf[T] => deriveProduct[T](x)
        case x: Mirror.SumOf[T]     => deriveSum[T](x)
      }
  }

  object EncoderDeriver extends Deriver {

    private val singletonEncoder = Encoder[Any]((w, _) => w.writeEmptyArray())

    inline def deriveSingleton[T](m: Mirror.Singleton): Encoder[T] =
      singletonEncoder.asInstanceOf[Encoder[T]]

    inline def deriveProduct[T](m: Mirror.ProductOf[T]): Encoder[T] =
      Encoder { (w, x) =>
        inline erasedValue[m.MirroredElemTypes] match {
          case _: EmptyTuple => writeMembers[T, m.MirroredElemTypes, m.MirroredElemLabels](w, x)
          case _: NonEmptyTuple =>
            inline val size = constValue[Tuple.Size[m.MirroredElemTypes]]
            writeMembers[T, m.MirroredElemTypes, m.MirroredElemLabels](w.writeArrayOpen(size), x).writeArrayClose()
        }
      }

    private inline def writeMembers[T, Elems <: Tuple, Labels <: Tuple](inline w: Writer, inline value: T): Writer =
      inline (erasedValue[Elems], erasedValue[Labels]) match {
        case _: (EmptyTuple, EmptyTuple) => w
        case _: (e *: es, l *: ls) =>
          writeMembers[T, es, ls](w.write(Util.select[T, e, l](value))(using summonInline[Encoder[e]]), value)
      }

    inline def deriveSum[T](m: Mirror.SumOf[T]): Encoder[T] =
      Encoder { (w, x) =>
        dispatch[T, m.MirroredElemTypes](w, x)
      }

    inline def dispatch[T, Elems <: Tuple](w: Writer, value: T): Writer = ${dispatchImpl[T, Elems]('w, 'value)}

    def dispatchImpl[T:Type, Elems <: Tuple :Type](w: Expr[Writer], value: Expr[T])(using quotes: Quotes): Expr[Writer] = {
      import quotes.reflect._
      val keyTypeRepr = TypeRepr.of[io.bullet.borer.derivation.key]
      val tTypeRepr = TypeRepr.of[T]
      val memberSymbols = tTypeRepr.typeSymbol.caseFields
      val keys: List[Option[Long | String]] = memberSymbols.map { symbol =>
        symbol
          .annotations
          .find(_.tpe =:= keyTypeRepr)
          .flatMap {
            case Apply(_, Literal(StringConstant(x)) :: Nil) => Some(x)
            case Apply(_, Literal(LongConstant(x)) :: Nil) => Some(x)
            case Apply(_, Literal(IntConstant(x)) :: Nil) => Some(x.toLong)
            case _ =>
              report.error(s"Illegal @key annotation on member $symbol of type  ${tTypeRepr.show}")
              None
          }
      }
      val cases: List[CaseDef] = caseDefs[Elems](w)
      Match(value.asTerm, cases).asExprOf[Writer]
    }

    inline def caseDefs[Elems <: Tuple: Type](w: Expr[Writer])(using q: Quotes): List[q.reflect.CaseDef] =
      Type.of[Elems] match {
        case '[ head *: tail ] =>
          Util.caseDef[head, Writer](x => '{ $w.write($x)(using summonInline[Encoder[head]]) }) :: caseDefs[tail](w)
        case '[ EmptyTuple ] => Nil
      }
  }

  def encoder[T:Type](using quotes: Quotes): Expr[Encoder[T]] = {
    import quotes.reflect.*

    Expr.summon[Mirror.Of[T]].get match {
      case '{ $m: Mirror.ProductOf[T] { type MirroredElemTypes = elementTypes }} => ???
      case '{ $m: Mirror.SumOf[T] { type MirroredElemTypes = elementTypes }} => ???
    }
  }

  def allEncoders[T](using quotes: Quotes): Expr[Encoder[T]] = ???

  def decoder[T](using quotes: Quotes): Expr[Decoder[T]] = ???

  def allDecoders[T](using quotes: Quotes): Expr[Decoder[T]] = ???

  def codec[T](using quotes: Quotes): Expr[Codec[T]] = ???

  def allCodecs[T](using quotes: Quotes): Expr[Codec[T]] = ???
}
