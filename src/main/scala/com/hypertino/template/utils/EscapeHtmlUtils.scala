package com.hypertino.template.utils

import scala.StringContext.treatEscapes
import scalatags.Escaping

trait EscapeHtmlUtils {
  implicit class InterpolatorWrapper(val stringContext: StringContext) {
    def escapeHtmlArgs(args: Any*): String = {
      val refinedArgs = args.map { arg =>
        if (arg != null && arg.isInstanceOf[Keep]){
          arg.source
        }else{
          escapeHtml(String.valueOf(arg))
        }
      }

      stringContext.standardInterpolator(treatEscapes, refinedArgs)
    }

  }

  implicit class Keep(val source: Any){
    def keepHtml(): Keep = this

    override def toString: String = String.valueOf(source)
  }

  def escapeHtml(string: String): String ={
    val stringBuilder = new StringBuilder()

    Escaping.escape(string, stringBuilder)

    stringBuilder.toString()
  }
}
