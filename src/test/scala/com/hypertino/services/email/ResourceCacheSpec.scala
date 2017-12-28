/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.services.email

import com.hypertino.template.utils.LanguageRanges
import org.scalatest.{FlatSpec, Matchers}

class ResourceCacheSpec extends FlatSpec with Matchers {
  "ResourceCache" should "load bundle according to language" in {
    import templates._

    r("color")(LanguageRanges("*")) shouldBe "color"
    r("color")(LanguageRanges("en-UK")) shouldBe "colour"
    r("color")(LanguageRanges("ru")) shouldBe "цвет"
    r("color")(LanguageRanges("fr-CH, ru;q=0.9, en-UK;q=0.8, de;q=0.7, *;q=0.5")) shouldBe "цвет"
    r("color")(LanguageRanges("ru-UA;q=0.9, en-UK;q=0.8, de;q=0.7, *;q=0.5")) shouldBe "цвет"
  }
}
