/*
 * Copyright (c) 2019-2021 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.derivation

import io.bullet.borer._
import utest._

import java.nio.charset.StandardCharsets

object MiscSpec extends AbstractBorerSpec {

  def encode[T: Encoder](value: T): String   = Json.encode(value).toUtf8String
  def decode[T: Decoder](encoded: String): T = Json.decode(encoded getBytes StandardCharsets.UTF_8).to[T].value

  final case class Foo(abc: Int, d: String, efghi: Boolean)

  val tests = Tests {

    "smoke" - {
      implicit val encoder: Encoder[Foo] = ArrayBasedCodecs.deriveEncoder
      verifyEncoding(Foo(42, "xxx", true), """[42,"xxx",true]""")
    }

  }
}
