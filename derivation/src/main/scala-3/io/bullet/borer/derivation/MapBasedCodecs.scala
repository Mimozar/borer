/*
 * Copyright (c) 2019-2021 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.derivation

import io.bullet.borer.{Codec, Decoder, Encoder}

import scala.quoted.{Expr, Quotes}

/**
 * Derivation macros for map-based encodings.
 */
object MapBasedCodecs {

  /**
   * Macro that creates an [[Encoder]] for [[T]] provided that
   * - [[T]] is a `case class`, `sealed abstract class` or `sealed trait`
   * - [[Encoder]] instances for all members of [[T]] (if [[T]] is a `case class`)
   *   or all sub-types of [[T]] (if [[T]] is an ADT) are implicitly available
   *
   * Case classes are converted into a map of values, one key-value pair for each member.
   * The key for each member is a `String` holding the member's name.
   * This can be customized with the [[key]] annotation.
   */
  inline def deriveEncoder[T]: Encoder[T] = ${Macros.encoder[T]}

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
  inline def deriveAllEncoders[T]: Encoder[T] = ${Macros.allEncoders[T]}

  /**
   * Macro that creates a [[Decoder]] for [[T]] provided that
   * - [[T]] is a `case class`, `sealed abstract class` or `sealed trait`
   * - [[Decoder]] instances for all members of [[T]] (if [[T]] is a `case class`)
   *   or all sub-types of [[T]] (if [[T]] is an ADT) are implicitly available
   *
   * Case classes are created from a map of values, one key-value pair for each member.
   * The key for each member is a `String` holding the member's name.
   * This can be customized with the [[key]] annotation.
   */
  inline def deriveDecoder[T]: Decoder[T] = ${Macros.decoder[T]}

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
  inline def deriveAllDecoders[T]: Decoder[T] = ${Macros.allDecoders[T]}

  /**
   * Macro that creates an [[Encoder]] and [[Decoder]] pair for [[T]].
   * Convenience shortcut for `Codec(deriveEncoder[T], deriveDecoder[T])"`.
   */
  inline def deriveCodec[T]: Codec[T] = ${Macros.codec[T]}

  /**
   * Macro that creates an [[Encoder]] and [[Decoder]] pair for [[T]] and all direct and indirect sub-types of [[T]].
   * Convenience shortcut for `Codec(deriveAllEncoders[T], deriveAllDecoders[T])"`.
   */
  inline def deriveAllCodecs[T]: Codec[T] = ${Macros.allCodecs[T]}

  private object Macros {

    def encoder[T](using quotes: Quotes): Expr[Encoder[T]] = ???

    def allEncoders[T](using quotes: Quotes): Expr[Encoder[T]] = ???

    def decoder[T](using quotes: Quotes): Expr[Decoder[T]] = ???

    def allDecoders[T](using quotes: Quotes): Expr[Decoder[T]] = ???

    def codec[T](using quotes: Quotes): Expr[Codec[T]] = ???

    def allCodecs[T](using quotes: Quotes): Expr[Codec[T]] = ???
  }
}
