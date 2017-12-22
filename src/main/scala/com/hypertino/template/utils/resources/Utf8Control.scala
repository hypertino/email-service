package com.hypertino.template.utils.resources

import java.io.{FileNotFoundException, InputStreamReader}
import java.util.{Locale, PropertyResourceBundle, ResourceBundle}
import java.util.ResourceBundle.Control

object Utf8Control extends Control {
  override def newBundle(baseName: String, locale: Locale, format: String, loader: ClassLoader, reload: Boolean): ResourceBundle = { // The below is a copy of the default implementation.
    val bundleName = toBundleName(baseName, locale)
    val resourceName = toResourceName(bundleName, "properties")
    val stream =
      if (reload) {
        val url = loader.getResource(resourceName)
        if (url != null && url.openConnection != null) {
          url.openConnection.setUseCaches(false)
          Some(url.openConnection.getInputStream)
        }
        else {
          None
        }
      }
      else {
        Some(loader.getResourceAsStream(resourceName))
      }

    stream.map { s ⇒
      try {
        new PropertyResourceBundle(new InputStreamReader(s, "UTF-8"))
      } finally {
        s.close()
      }
    } getOrElse {
      throw new FileNotFoundException(s"Resource $baseName for locale $locale is not found")
    }
  }
}