/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.services.email

import javax.mail.internet.InternetAddress

import com.hypertino.binders.value.Obj
import com.hypertino.hyperbus.Hyperbus
import com.hypertino.hyperbus.model.MessagingContext
import com.hypertino.hyperbus.subscribe.Subscribable
import com.hypertino.hyperbus.transport.api.ServiceRegistrator
import com.hypertino.hyperbus.transport.registrators.DummyRegistrator
import com.hypertino.service.config.ConfigLoader
import com.hypertino.services.email.api.EmailsPost
import com.typesafe.config.Config
import monix.execution.Scheduler
import org.jvnet.mock_javamail.Mailbox
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import scaldi.Module

import scala.concurrent.duration._

class EmailServiceSpec extends FlatSpec with Module with BeforeAndAfterAll with ScalaFutures with Matchers with Subscribable {
  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(3, Seconds)))
  implicit val scheduler = monix.execution.Scheduler.Implicits.global
  implicit val mcx = MessagingContext.empty
  bind [Config] to ConfigLoader()
  bind [Scheduler] to scheduler
  bind [Hyperbus] to injected[Hyperbus]
  bind [ServiceRegistrator] to DummyRegistrator

  val hyperbus = inject[Hyperbus]
  Thread.sleep(500)

  val service = new EmailService()

  override def afterAll() {
    service.stopService(false, 10.seconds).futureValue
    hyperbus.shutdown(10.seconds).runAsync.futureValue
  }

  "EmailService" should "send email" in {
    val c = hyperbus
      .ask(EmailsPost(api.EmailMessage("test-email", None, Obj.from(
        "user" → Obj.from(
          "email" → "john@example.com",
          "name" → "John"
        )
      ))))
      .runAsync
      .futureValue

    val inbox = Mailbox.get("john@example.com")
    inbox.size shouldBe 1
    val msg = inbox.get(0)
    msg.getSubject shouldBe "Hello"
    msg.getAllRecipients.map(_.asInstanceOf[InternetAddress].getAddress) should contain("john@example.com")
    msg.getContent shouldBe "<p>Hello <strong>John</strong><hr />How are you?<a href=\"http://example.net/abcde\">read more</a><p>color: color.&quot;'&quot;</p></p>"
    inbox.clear()
  }

  it should "send email according to language" in {
    val c = hyperbus
      .ask(EmailsPost(api.EmailMessage("test-email", Some("en-uk"), Obj.from(
        "user" → Obj.from(
          "email" → "boris@example.com",
          "name" → "Boris"
        )
      ))))
      .runAsync
      .futureValue

    val inbox = Mailbox.get("boris@example.com")
    inbox.size shouldBe 1
    val msg = inbox.get(0)
    msg.getSubject shouldBe "Hello"
    msg.getAllRecipients.map(_.asInstanceOf[InternetAddress].getAddress) should contain("boris@example.com")
    msg.getContent shouldBe "<p>Hello <strong>Boris</strong><hr />How are you?<a href=\"http://example.net/abcde\">read more</a><p>color: colour.&quot;'&quot;</p></p>"
    inbox.clear()
  }

  it should "send email according to language with utf-8 chars" in {
    val c = hyperbus
      .ask(EmailsPost(api.EmailMessage("test-email", Some("ru"), Obj.from(
        "user" → Obj.from(
          "email" → "boris@example.com",
          "name" → "Boris"
        )
      ))))
      .runAsync
      .futureValue

    val inbox = Mailbox.get("boris@example.com")
    inbox.size shouldBe 1
    val msg = inbox.get(0)
    msg.getSubject shouldBe "Hello"
    msg.getAllRecipients.map(_.asInstanceOf[InternetAddress].getAddress) should contain("boris@example.com")
    msg.getContent shouldBe "<p>Hello <strong>Boris</strong><hr />How are you?<a href=\"http://example.net/abcde\">read more</a><p>color: цвет.&quot;'&quot;</p></p>"
    inbox.clear()
  }
  
  it should "send email by default language if not matched with any requested accept-language" in {
    val c = hyperbus
      .ask(EmailsPost(api.EmailMessage("test-email", Some("fr-CH, de;q=0.9, it;q=0.8"), Obj.from(
        "user" → Obj.from(
          "email" → "boris@example.com",
          "name" → "Boris"
        )
      ))))
      .runAsync
      .futureValue
    
    val inbox = Mailbox.get("boris@example.com")
    inbox.size shouldBe 1
    val msg = inbox.get(0)
    msg.getSubject shouldBe "Hello"
    msg.getAllRecipients.map(_.asInstanceOf[InternetAddress].getAddress) should contain("boris@example.com")
    msg.getContent shouldBe "<p>Hello <strong>Boris</strong><hr />How are you?<a href=\"http://example.net/abcde\">read more</a><p>color: color.&quot;'&quot;</p></p>"
    inbox.clear()
  }
  
  it should "send email by the most appropriate language according accept-language quality-factor" in {
    val c = hyperbus
      .ask(EmailsPost(api.EmailMessage("test-email", Some("fr-CH, ru;q=0.9, en-UK;q=0.8, de;q=0.7, *;q=0.5"), Obj.from(
        "user" → Obj.from(
          "email" → "boris@example.com",
          "name" → "Boris"
        )
      ))))
      .runAsync
      .futureValue
    
    val inbox = Mailbox.get("boris@example.com")
    inbox.size shouldBe 1
    val msg = inbox.get(0)
    msg.getSubject shouldBe "Hello"
    msg.getAllRecipients.map(_.asInstanceOf[InternetAddress].getAddress) should contain("boris@example.com")
    msg.getContent shouldBe "<p>Hello <strong>Boris</strong><hr />How are you?<a href=\"http://example.net/abcde\">read more</a><p>color: цвет.&quot;'&quot;</p></p>"
    inbox.clear()
  }
  
}
