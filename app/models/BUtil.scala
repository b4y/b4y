package models

import play.modules.mailer._
import javax.mail.Message
import play.api.mvc.Session
import controllers.Application


object BUtil {
  val isTest = false

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

  def getUser(session: Session):User = {
    val userIdOption = session.get(Application.SessionNameUserId)
    if (userIdOption.isEmpty)
      throw new IllegalStateException("No " + Application.SessionNameUserId + "found in session")
    val userId = userIdOption.get
    val user = User.load(userId)
    if (null == user)
      throw new IllegalStateException("No user found in db for userId" + userId)
    user
  }
}
