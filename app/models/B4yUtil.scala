package models

import play.modules.mailer._
import javax.mail.Message


object B4yUtil {
  val isTest = true

  def sendEmail(recipientEmail: String, recipientName: String) {
    val sender = "jigang_hao@hotmail.com"
    Mailer.sendEmail(Email(
      subject = "Welcome " + recipientName + "!",
      from = EmailAddress("B4y", sender),
      replyTo = None,
      recipients = List(Recipient(Message.RecipientType.TO, EmailAddress(recipientName, recipientEmail))),
      text = "text",
      htmlText = "Welcome to b4y",
      attachments = Seq.empty))
  }
}
