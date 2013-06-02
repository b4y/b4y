package util

import play.modules.mailer._
import javax.mail.Message
import play.api.mvc.Session
import controllers.Application
import java.util.Date
import models.ProductItem.PriceAtTime
import scala.collection.JavaConverters._
import models.{User, ProductItem}


object BUtil {
  val isTest = false
  val mockUpItems = List(
    ProductItem("On China", "0143121316", "", "http://ecx.images-amazon.com/images/I/41nPFVINbhL._SL160_.jpg", List(PriceAtTime(available = true, price = 950, date = new Date)).asJava),
    ProductItem("DK Eyewitness Travel Guide: China", "0756684307", "", "http://ecx.images-amazon.com/images/I/51Xy2XNo2YL._SL160_.jpg", List(PriceAtTime(available = true, price = 1835, date = new Date)).asJava),
    ProductItem("Lonely Planet China (Travel Guide)", "1742201385", "", "http://ecx.images-amazon.com/images/I/51NK2%2B-q81L._SL160_.jpg", List(PriceAtTime(available = true, price = 2165, date = new Date)).asJava))

  //  def createSignUpEmailText(firstName:String, userId:String) = {
//    val htmlText = "Welcome to b4y, " + firstName + "! <br> Plelase click the link below to activate your account: <br>" +
//      "http://localhost:9000/activateAccount"
//  }
  def sendEmail(recipientEmail: String, recipientName: String,  htmlText:String = "Welcome to b4y") {
    val sender = "jigang_hao@hotmail.com"
    Mailer.sendEmail(Email(
      subject = "Welcome " + recipientName + "!",
      from = EmailAddress("B4y", sender),
      replyTo = None,
      recipients = List(Recipient(Message.RecipientType.TO, EmailAddress(recipientName, recipientEmail))),
      text = "text",
      htmlText = htmlText,
      attachments = Seq.empty))
  }

  def getUser(session: Session):User = {
    val userIdOption = session.get(Application.SessionNameUserId)
    if (userIdOption.isEmpty)
      throw new IllegalStateException("No " + Application.SessionNameUserId + "found in session")
    val userId = userIdOption.get
    getUser(userId)
  }

  def getUser(userId: String):User = {
    val user = User.load(userId)
    if (null == user)
      throw new IllegalStateException("No user found in db for userId" + userId)
    user
  }
}
