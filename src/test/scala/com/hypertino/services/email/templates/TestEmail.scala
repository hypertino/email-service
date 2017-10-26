package com.hypertino.services.email.templates
import scalatags.Text.all._

class TestEmail($: Value) extends Email(
  recipients  = Seq(($("user.email"), $("user.name"))),
  subject = "Hello",
  html = p(
    "Hello ", strong($("user.name")),
    hr,
    "How are you?"
  )
)

