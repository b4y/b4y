package util

import play.modules.mailer.{Recipient, EmailAddress, Email, Mailer}
import javax.mail.Message

object EmailUtil {
  def sendSignUpEmail(address: String, firstName: String, lastName: String, userId: String) {
    val subject = "Welcome to b4y " + firstName + "!"
    val text = "Welcome to b4y, " + firstName + "! <br> Plelase click the link below to activate your account: <br>" +
      "<a href=\"http://localhost:9000/activateAccount/"+ userId+"\">" + "Activate accont</a>"
    this.sendEmail(address, firstName + " " + lastName, subject, text)
  }

  private final def sendEmail(recipientEmail: String, recipientName: String, subject: String, htmlText: String) {
    val sender = "jigang_hao@hotmail.com"
    Mailer.sendEmail(Email(
      subject = subject,
      from = EmailAddress("B4y", sender),
      replyTo = None,
      recipients = List(Recipient(Message.RecipientType.TO, EmailAddress(recipientName, recipientEmail))),
      text = "text",
      htmlText = htmlText,
      attachments = Seq.empty))
  }
}
