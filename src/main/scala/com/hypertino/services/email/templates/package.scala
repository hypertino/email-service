package com.hypertino.services.email

import com.hypertino.langutils.ResourceCache
import com.hypertino.template.utils.ScalatagValue

package object templates extends ScalatagValue with ResourceCache {
  override def bundleName: String = "resources"
  type LanguageRanges = com.hypertino.langutils.LanguageRanges
}
