package com.hypertino.services.email

import scalatags.Text._
import scalatags.generic.TypedTag
import scalatags.{DataConverters, text}

// todo: support resource reference for l10n
// example r("button_name") returns localized resource

package object templates {

  implicit def value2string(v: Value): String = v.toString()
  implicit def value2stringo(v: Value): Option[String] = v match {
    case Null ⇒ None
    case o ⇒ Some(o.toString())
  }

  implicit def string2stringo(s: String): Option[String] = if (s != null && s.trim.length > 0) Some(s) else None
  implicit def tag2string(t: TypedTag[_, _, _]): String = t.toString
  implicit def tag2stringo(t: TypedTag[_, _, _]): Option[String] = string2stringo(t.toString)

  implicit def valueFrag(v: Value): Frag = v match {
    case Null ⇒ StringFrag("")
    case o ⇒ StringFrag(o.toString)
  }

  type Value=com.hypertino.binders.value.Value
  type Obj=com.hypertino.binders.value.Obj
  type Lst=com.hypertino.binders.value.Lst
  type Bool=com.hypertino.binders.value.Bool
  type EmailMessage = com.hypertino.services.email.EmailMessage
  val Null=com.hypertino.binders.value.Null
}
