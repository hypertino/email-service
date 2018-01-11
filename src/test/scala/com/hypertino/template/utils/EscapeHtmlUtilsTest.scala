package com.hypertino.template.utils

import org.scalatest.{FlatSpec, Matchers}

class EscapeHtmlUtilsTest extends FlatSpec
  with Matchers
  with EscapeHtmlUtils {
  "escapeHtmlArgs" should "interpolate normal text" in {
    escapeHtmlArgs"<div><p>${"just text"}</p><div>" shouldBe "<div><p>just text</p><div>"
  }

  it should "escape tags" in {
    escapeHtmlArgs"<div><p>${"<small>tagged text</small>"}</p><div>" shouldBe "<div><p>&lt;small&gt;tagged text&lt;/small&gt;</p><div>"
  }

  it should "interpolate kept args" in {
    escapeHtmlArgs"<div><p>${"<small>tagged text</small>".keepHtml()}</p><div>" shouldBe "<div><p><small>tagged text</small></p><div>"
  }
}
