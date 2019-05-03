/*
 * Copyright (c) 2019 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.derivation

import io.bullet.borer.{Cbor, Dom}

object CborDerivationSpec extends DerivationSpec(Cbor) {
  import Dom._

  def arrayBasedFooDom =
    ArrayElem.Sized(
      IntElem(120),
      IntElem(66),
      IntElem(-10000),
      IntElem(1234567),
      IntElem(-1),
      Float16Elem(1.5f),
      DoubleElem(26.8),
      StringElem("borer"),
      ArrayElem.Sized(),
      ArrayElem.Unsized(
        ArrayElem.Sized(IntElem(255), IntElem(0), IntElem(0), IntElem(255)),
        ArrayElem.Sized(IntElem(0), IntElem(255), IntElem(0), IntElem(255)),
        ArrayElem.Sized(IntElem(0), IntElem(0), IntElem(255), IntElem(255))
      ))

  def arrayBasedMissingElemErrorMsg =
    "Expected Array Start or Array Header(10) for decoding an instance of type " +
      "[io.bullet.borer.derivation.DerivationSpec.Foo] but got Array Header (9) [input position 0]"

  def mapBasedFooDom =
    MapElem.Sized(
      "char"   → IntElem(120),
      "byte"   → IntElem(66),
      "short"  → IntElem(-10000),
      "int"    → IntElem(1234567),
      "long"   → IntElem(-1),
      "float"  → Float16Elem(1.5f),
      "double" → DoubleElem(26.8),
      "string" → StringElem("borer"),
      "empty"  → MapElem.Sized(),
      "colors" → ArrayElem.Unsized(
        MapElem.Sized("red" → IntElem(255), "green" → IntElem(0), "blue"   → IntElem(0), "alpha"   → IntElem(255)),
        MapElem.Sized("red" → IntElem(0), "green"   → IntElem(255), "blue" → IntElem(0), "alpha"   → IntElem(255)),
        MapElem.Sized("red" → IntElem(0), "green"   → IntElem(0), "blue"   → IntElem(255), "alpha" → IntElem(255))
      ))

  def mapBased100Dom =
    MapElem.Sized(
      "x00" → BoolElem(false),
      "x01" → IntElem(120),
      "x02" → IntElem(66),
      "x03" → IntElem(-10000),
      "x04" → IntElem(1234567),
      "x05" → LongElem(2147483648l),
      "x06" → Float16Elem(1.5f),
      "x07" → DoubleElem(26.8),
      "x08" → StringElem("borer"),
      "x09" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x10" → BoolElem(false),
      "x11" → IntElem(120),
      "x12" → IntElem(66),
      "x13" → IntElem(-10000),
      "x14" → IntElem(1234567),
      "x15" → LongElem(2147483648l),
      "x16" → Float16Elem(1.5f),
      "x17" → DoubleElem(26.8),
      "x18" → StringElem("borer"),
      "x19" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x20" → BoolElem(false),
      "x21" → IntElem(120),
      "x22" → IntElem(66),
      "x23" → IntElem(-10000),
      "x24" → IntElem(1234567),
      "x25" → LongElem(2147483648l),
      "x26" → Float16Elem(1.5f),
      "x27" → DoubleElem(26.8),
      "x28" → StringElem("borer"),
      "x29" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x30" → BoolElem(false),
      "x31" → IntElem(120),
      "x32" → IntElem(66),
      "x33" → IntElem(-10000),
      "x34" → IntElem(1234567),
      "x35" → LongElem(2147483648l),
      "x36" → Float16Elem(1.5f),
      "x37" → DoubleElem(26.8),
      "x38" → StringElem("borer"),
      "x39" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x40" → BoolElem(false),
      "x41" → IntElem(120),
      "x42" → IntElem(66),
      "x43" → IntElem(-10000),
      "x44" → IntElem(1234567),
      "x45" → LongElem(2147483648l),
      "x46" → Float16Elem(1.5f),
      "x47" → DoubleElem(26.8),
      "x48" → StringElem("borer"),
      "x49" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x50" → BoolElem(false),
      "x51" → IntElem(120),
      "x52" → IntElem(66),
      "x53" → IntElem(-10000),
      "x54" → IntElem(1234567),
      "x55" → LongElem(2147483648l),
      "x56" → Float16Elem(1.5f),
      "x57" → DoubleElem(26.8),
      "x58" → StringElem("borer"),
      "x59" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x60" → BoolElem(false),
      "x61" → IntElem(120),
      "x62" → IntElem(66),
      "x63" → IntElem(-10000),
      "x64" → IntElem(1234567),
      "x65" → LongElem(2147483648l),
      "x66" → Float16Elem(1.5f),
      "x67" → DoubleElem(26.8),
      "x68" → StringElem("borer"),
      "x69" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x70" → BoolElem(false),
      "x71" → IntElem(120),
      "x72" → IntElem(66),
      "x73" → IntElem(-10000),
      "x74" → IntElem(1234567),
      "x75" → LongElem(2147483648l),
      "x76" → Float16Elem(1.5f),
      "x77" → DoubleElem(26.8),
      "x78" → StringElem("borer"),
      "x79" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x80" → BoolElem(false),
      "x81" → IntElem(120),
      "x82" → IntElem(66),
      "x83" → IntElem(-10000),
      "x84" → IntElem(1234567),
      "x85" → LongElem(2147483648l),
      "x86" → Float16Elem(1.5f),
      "x87" → DoubleElem(26.8),
      "x88" → StringElem("borer"),
      "x89" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
      "x90" → BoolElem(false),
      "x91" → IntElem(120),
      "x92" → IntElem(66),
      "x93" → IntElem(-10000),
      "x94" → IntElem(1234567),
      "x95" → LongElem(2147483648l),
      "x96" → Float16Elem(1.5f),
      "x97" → DoubleElem(26.8),
      "x98" → StringElem("borer"),
      "x99" → MapElem.Sized("red" → IntElem(0), "green" → IntElem(0), "blue" → IntElem(0), "alpha" → IntElem(255)),
    )

  def animalsDom =
    ArrayElem.Unsized(
      ArrayElem.Sized(StringElem("Dog"), ArrayElem.Sized(IntElem(12), StringElem("Fred"))),
      ArrayElem
        .Sized(StringElem("TheCAT"), ArrayElem.Sized(Float16Elem(1.0f), StringElem("none"), StringElem("there"))),
      ArrayElem.Sized(StringElem("Dog"), ArrayElem.Sized(IntElem(4), StringElem("Lolle"))),
      ArrayElem.Sized(IntElem(42), BoolElem.True))
}
