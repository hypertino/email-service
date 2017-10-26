package com.hypertino.services.email

import com.hypertino.template.utils.{ResourceCache, ScalatagValue}

package object templates extends ScalatagValue with ResourceCache {
  override def bundleName: String = "resources"
  type LanguageRanges = com.hypertino.template.utils.LanguageRanges
}
