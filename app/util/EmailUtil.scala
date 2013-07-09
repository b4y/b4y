package util

import play.modules.mailer.{Recipient, EmailAddress, Email, Mailer}
import javax.mail.Message
import models.UserWithProductItems.UserItemWithProductItem
import models.UserItemMatched

object EmailUtil {
  val host = "http://hooraySave.com"
  val companyName = "HooraySave.com"
  def sendSignUpEmail(address: String, firstName: String, lastName: String, userId: String) {
    val subject = "Welcome to " + companyName + " " + firstName + "!"
    val text = subject+ "<br> Plelase click the link below to activate your account: <br>" +
      "<a href=" + host + "/activateAccount/"+ userId+ ">" + "Activate accont</a>"
    this.sendEmail(address, firstName + " " + lastName, subject, text)
  }

  def sendResetPasswordEmail(address: String, firstName: String, lastName: String, userId: String, password:String) {
    val subject = "Reset your HooraySave password"
    val text = firstName + ": <br> Your new " + companyName + " password is: " + password + "<br>" +
      "You can click the link below to sign in your hooraySave account: <br><a href=" + host + ">HooraySave.com</a>"
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
    "Enjoy!<br><br>\n\n" + companyName + " team"
    this.sendEmail(user.email, user.firstName + " " + user.lastName, subject, text)
  }

  private final def sendEmail(recipientEmail: String, recipientName: String, subject: String, htmlText: String) {
    val sender = "support@hooraysave.com"
    Mailer.sendEmail(Email(
      subject = subject,
      from = EmailAddress(sender, sender),
      replyTo = None,
      recipients = List(Recipient(Message.RecipientType.TO, EmailAddress(recipientName, recipientEmail))),
      text = "text",
      htmlText = htmlText,
      attachments = Seq.empty))
  }
}
