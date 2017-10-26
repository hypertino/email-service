package com.hypertino.template.utils

import java.util.concurrent.Callable
import java.util.{Locale, ResourceBundle}

import com.google.common.cache.{Cache, CacheBuilder}

import scala.util.Try

trait ResourceCache {
  protected def bundleName: String
  protected val defaultLocale: Locale = Locale.getDefault
  protected val resourceCache: Cache[String, Option[ResourceBundle]] = CacheBuilder
    .newBuilder()
    .build[String, Option[ResourceBundle]]()

  def locale(implicit languageRanges: LanguageRanges): Locale = lookupBundle.getLocale

  def r(s: String)(implicit languageRanges: LanguageRanges): String = lookupBundle.getString(s)

  protected def lookupBundle(implicit languageRanges: LanguageRanges): ResourceBundle = {
    languageRanges
      .languages
      .flatMap { l ⇒
        val lang = if (l.range == "*") defaultLocale.getLanguage else l.range
        resourceCache.get(lang, new Callable[Option[ResourceBundle]] {
          override def call() = Try(loadBundle(lang)).toOption
        })
      }
      .headOption
      .getOrElse {
        if (languageRanges.raw != "*")
          lookupBundle(LanguageRanges("*"))
        else {
          throw new RuntimeException(s"Resource bundle $bundleName is not found")
        }
      }
  }

  protected def loadBundle(language: String): ResourceBundle = {
    val locale = Locale.forLanguageTag(language)
    ResourceBundle.getBundle(bundleName, locale,
      ResourceBundle.Control.getNoFallbackControl(
        ResourceBundle.Control.FORMAT_DEFAULT))
  }
}
