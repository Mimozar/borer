/*
 * Copyright (c) 2019-2021 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer

final case class Default[+T](defaultValue: T)

object Default:
  implicit val boolean: Default[Boolean] = Default(false)
  implicit val byte: Default[Byte]       = Default(0: Byte)
  implicit val short: Default[Short]     = Default(0: Short)
  implicit val int: Default[Int]         = Default(0)
  implicit val long: Default[Long]       = Default(0L)
  implicit val string: Default[String]   = Default("")
  implicit val float: Default[Float]     = Default(0.0f)
  implicit val double: Default[Double]   = Default(0.0)

  @inline def of[T](implicit d: Default[T]): T = d.defaultValue

  @inline def orValue[T: Default](value: T): T = if value == null then Default.of[T] else value

  private[this] val optionDefault            = Default(None)
  implicit def option[T]: Default[Option[T]] = optionDefault
