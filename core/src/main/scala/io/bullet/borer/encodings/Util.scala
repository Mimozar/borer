/*
 * Copyright (c) 2019-2022 Mathias Doenitz
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.bullet.borer.encodings

private object Util:

  def show(chars: Array[Char]): String =
    if (chars.length <= 16) new String(chars) else new String(chars.take(16)) + "..."
