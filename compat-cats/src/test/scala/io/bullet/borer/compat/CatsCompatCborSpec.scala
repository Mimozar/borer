/*
 * Copyright (c) 2019-2022 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.compat

import _root_.cats.data._
import _root_.cats.instances.string._
import _root_.cats.instances.int._
import io.bullet.borer._

class CatsCompatCborSpec extends ByteArrayCborSpec {
  import cats._

  test("Chain") {
    roundTrip("9f010203ff", Chain(1, 2, 3))
  }

  test("Ior") {
    roundTrip("9f820001820163666f6f8302182a63626172ff", List(Ior.Left(1), Ior.Right("foo"), Ior.Both(42, "bar")))
  }

  test("NonEmptyChain") {
    roundTrip("9f010203ff", NonEmptyChain(1, 2, 3))
  }

  test("NonEmptyList") {
    roundTrip("9f010203ff", NonEmptyList.of(1, 2, 3))
  }

  test("NonEmptyMap") {
    roundTrip(
      "a364626c75651a000186a065677265656e1904d26372656412",
      NonEmptyMap.of("red" -> 18, "green" -> 1234, "blue" -> 100000))
  }

  test("NonEmptySet") {
    roundTrip("9f010203ff", NonEmptySet.of(1, 2, 3))
  }

  test("NonEmptyVector") {
    roundTrip("83010203", NonEmptyVector.of(1, 2, 3))
  }

  test("Validated") {
    roundTrip("9fa100626e6fa101182aff", List(Validated.Invalid("no"), Validated.Valid(42)))
  }
}
