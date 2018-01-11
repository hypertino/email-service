package com.hypertino.services.email

import com.hypertino.langutils.ResourceCache
import com.hypertino.template.utils.{ScalatagValue, EscapeHtmlUtils}

package object templates extends ScalatagValue with ResourceCache with EscapeHtmlUtils {
  override def bundleName: String = "resources"
  type LanguageRanges = com.hypertino.langutils.LanguageRanges

  def rHtml(s: String)(implicit languageRanges: LanguageRanges): Keep = lookupBundle.getString(s).keepHtml()
}
