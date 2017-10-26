package com.hypertino.template.utils

import java.util.Locale

case class LanguageRange(range: String, weight: Double)

case class LanguageRanges(raw: String) {
  import scala.collection.JavaConverters._

  lazy val languages: Seq[LanguageRange] = Locale.LanguageRange.parse(raw)
    .asScala
    .map { l ⇒
      LanguageRange(l.getRange, l.getWeight)
    }
}

object LanguageRanges {
  val default = LanguageRanges("*")
}