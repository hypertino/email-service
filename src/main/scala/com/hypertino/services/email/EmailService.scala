/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.services.email

import java.util.Properties
import java.util.concurrent.{Callable, TimeUnit}
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Message, PasswordAuthentication, Session, Transport}

import com.google.common.cache.{Cache, CacheBuilder}
import com.hypertino.binders.value.Value
import com.hypertino.hyperbus.Hyperbus
import com.hypertino.hyperbus.model.{Accepted, BadRequest, DynamicBody, EmptyBody, ErrorBody}
import com.hypertino.hyperbus.subscribe.Subscribable
import com.hypertino.inflector.naming.DashCaseToPascalCaseConverter
import com.hypertino.service.control.api.Service
import com.hypertino.services.email.apiref.email
import com.hypertino.services.email.apiref.email.EmailsPost
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import monix.execution.Scheduler
import scaldi.{Injectable, Injector}

import scala.concurrent._
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

case class EmailServiceConfig(
                              smtpHost: String,
                              smtpPort: Option[Int],
                              smtpUser: Option[String],
                              smtpPassword: Option[String],
                              sender: Option[String],
                              senderName: Option[String]
                             )

class EmailService(implicit val injector: Injector) extends Service with Injectable with Subscribable with StrictLogging {
  private implicit val scheduler = inject[Scheduler]
  private val hyperbus = inject[Hyperbus]
  private val templateCache = CacheBuilder
    .newBuilder()
    .build[(String,Option[String]), Option[Class[_]]]()
  import com.hypertino.binders.config.ConfigBinders._
  val config = inject[Config].read[EmailServiceConfig]("email")
  lazy val session = createEmailSession()

  logger.info(s"${getClass.getName} is STARTED")

  private val handlers = hyperbus.subscribe(this, logger)

  def onEmailsPost(implicit p: EmailsPost): Task[Accepted[DynamicBody]] = {
    runTemplate(p.body).map { e ⇒
      val from = e.from
        .map(f ⇒ (f, e.fromName))
        .orElse(
          config.sender.map(f ⇒ (f, config.senderName))
        )

      from.map { sender ⇒
        Task.eval {
          val m = new MimeMessage(session)
          m.setSubject(e.subject)
          m.setFrom(address(sender))
          e.recipients.foreach { r ⇒
            m.addRecipient(Message.RecipientType.TO, address(r))
          }
          e.ccRecipients.foreach { r ⇒
            m.addRecipient(Message.RecipientType.TO, address(r))
          }
          e.bccRecipients.foreach { r ⇒
            m.addRecipient(Message.RecipientType.TO, address(r))
          }
          e.html.foreach(s ⇒ m.setContent(s, "text/html"))
          e.text.foreach(s ⇒ m.setText(s))
          blocking(Transport.send(m))
          Accepted(EmptyBody)
        }
      } getOrElse {
        Task.raiseError(BadRequest(ErrorBody("sender-is-not-specified", Some(s"Sender is not specified (and no default is configured)"))))
      }
    } getOrElse {
      Task.raiseError(BadRequest(ErrorBody("template-not-found", Some(s"Template ${p.body.template} wasn't found"))))
    }
  }

  private def runTemplate(body: email.EmailMessage): Option[EmailMessage] = {
    // todo: parse language: AcceptLanguage and cache all classes
    val templateClass = templateCache.get((body.template,None), new Callable[Option[Class[_]]] {
      override def call() = {
        val className = "com.hypertino.services.email.templates." + DashCaseToPascalCaseConverter.convert(body.template)
        Try(Class.forName(className)).toOption
      }
    })

    templateClass.map { tc ⇒
      val args: Array[Object] = Array(body.data.asInstanceOf[AnyRef])
      val c = tc.getConstructor(classOf[Value])
      c.newInstance(args:_*).asInstanceOf[EmailMessage]
    }
  }

  private def createEmailSession() = {
    val props = new Properties(System.getProperties)
    props.setProperty("mail.smtp.host", config.smtpHost)

    val authenticator = config.smtpUser.map { userName ⇒
      props.setProperty("mail.smtp.auth", "true")
      new PasswordAuthentication(userName, config.smtpPassword.getOrElse(""))
    } getOrElse {
      null
    }

    config.smtpPort.foreach(port ⇒ props.setProperty("mail.smtp.port", port.toString))
    Session.getInstance(props)
  }

  private def address(a: (String, Option[String])) = a._2.map { n ⇒
    new InternetAddress(a._1, n)
  } getOrElse {
    new InternetAddress(a._1)
  }

  override def stopService(controlBreak: Boolean, timeout: FiniteDuration): Future[Unit] = Future {
    handlers.foreach(_.cancel())
    logger.info(s"${getClass.getName} is STOPPED")
  }
}
