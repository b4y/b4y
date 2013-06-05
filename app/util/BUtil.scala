package util

import play.modules.mailer._
import javax.mail.Message
import play.api.mvc.Session
import controllers.Application
import java.util.Date
import models.ProductItem.PriceAtTime
import scala.collection.JavaConverters._
import models.{User, ProductItem}
import org.jsoup.Jsoup


object BUtil {
  val isTest = false
  val mockUpItems = List(
    ProductItem("On China", "0143121316", "", "http://ecx.images-amazon.com/images/I/41nPFVINbhL._SL160_.jpg", List(PriceAtTime(available = true, price = 950, date = new Date)).asJava),
    ProductItem("DK Eyewitness Travel Guide: China", "0756684307", "", "http://ecx.images-amazon.com/images/I/51Xy2XNo2YL._SL160_.jpg", List(PriceAtTime(available = true, price = 1835, date = new Date)).asJava),
    ProductItem("Lonely Planet China (Travel Guide)", "1742201385", "", "http://ecx.images-amazon.com/images/I/51NK2%2B-q81L._SL160_.jpg", List(PriceAtTime(available = true, price = 2165, date = new Date)).asJava))



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

  def encrypt(passwordNotCoded: String) = {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    // add a salt. can replace salt with generated salt value
    val v = "salt" + passwordNotCoded
    // return encoded value
    new sun.misc.BASE64Encoder().encode(md.digest(v.getBytes("UTF-8")))
  }

  def curl(url:String) = {
    val doc = Jsoup.connect(url.replace(" ", "+")).userAgent("Mozilla").get()
    val bbb = doc.select("span:matchesOwn(Showing results)")
    val aaa = bbb.parents().get(1).children().first().children().get(1).getAllElements.first().text()
    aaa
  }
}
