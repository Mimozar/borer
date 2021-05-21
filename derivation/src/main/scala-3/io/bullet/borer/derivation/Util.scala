/*
 * Copyright (c) 2019-2021 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.derivation

import io.bullet.borer.{AdtEncoder, Encoder, Writer}

import scala.quoted._

object Util {

  inline def select[A, B, L](value: A): B = ${selectImpl[A, B, L]('value)}

  def selectImpl[A, B:Type, L :Type](value: Expr[A])(using quotes: Quotes): Expr[B] = {
    import quotes.reflect._
    Select.unique(value.asTerm, Type.valueOfConstant[L].get.asInstanceOf[String]).asExprOf[B]
  }

  def caseDef[A:Type, B:Type](branch: Expr[A] => Expr[B])(using q: Quotes): q.reflect.CaseDef = {
    val expr = '{
      (null :Any) match {
        case x: A => ${branch('x)}
      }
    }
    import q.reflect._
    val Inlined(_, _, Match(_, caseDef :: Nil)) = expr.asTerm
    caseDef
  }
}
