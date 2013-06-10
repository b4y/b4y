package util

import play.modules.mailer.{Recipient, EmailAddress, Email, Mailer}
import javax.mail.Message
import models.UserWithProductItems.UserItemWithProductItem
import models.UserItemMatched

object EmailUtil {
  def sendSignUpEmail(address: String, firstName: String, lastName: String, userId: String) {
    val subject = "Welcome to b4y " + firstName + "!"
    val text = "Welcome to b4y, " + firstName + "! <br> Plelase click the link below to activate your account: <br>" +
      "<a href=\"http://localhost:9000/activateAccount/"+ userId+"\">" + "Activate accont</a>"
    this.sendEmail(address, firstName + " " + lastName, subject, text)
  }

  def sendPriceMatchEmail(userItemMatched: UserItemMatched) {
    val user = userItemMatched.user
    val item = userItemMatched.item
    val subject = "Deal alert: " + item.name + " at $" + userItemMatched.priceMatched.toFloat/100
    val text = "Good news " + user.firstName + ", we find deal for you!<br>\n Here is the details:<br><br>\n" +
      "item name: " + item.name + " <br>" +
      "price you asked: $" + userItemMatched.priceExpected.toFloat/100 + " <br>" +
      "price we found for you: $" + userItemMatched.priceMatched.toFloat/100 + " <br><br>" +
      "Hurry up before the deal is gone! Click the link below to buy: <br>\n" +
      "<a href=\"" + item.detailPageURL + "\">" + "Buy " + item.name + "</a><br><br>"+
    "Enjoy!<br><br>\n\nB4y"
    this.sendEmail(user.email, user.firstName + " " + user.lastName, subject, text)
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
