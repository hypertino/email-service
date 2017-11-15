package com.hypertino.services.email.templates

import scalatags.Text.all._

class TestEmail($: Value, implicit val l: LanguageRanges) extends Email(
  recipients  = Seq(($("user.email"), $("user.name"))),
  subject = "Hello",
  html = html(raw(s"""<p>raw content: ${$("user.name")}</p>"""))
)

