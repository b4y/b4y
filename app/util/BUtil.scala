package util

import play.api.mvc.Session
import controllers.Application
import scala.collection.JavaConverters._
import models.User
import org.jsoup.Jsoup
import models.merchant.Deal

object BUtil {
  val SuperUserJim = "jigang_hao@hotmail.com"
  val isTest = false
  val mockUpItems = List()
//    ProductItem("On China", "0143121316", "", "http://ecx.images-amazon.com/images/I/41nPFVINbhL._SL160_.jpg", List(PriceAtTime(price = 950, date = new Date)).asJava),
//    ProductItem("DK Eyewitness Travel Guide: China", "0756684307", "", "http://ecx.images-amazon.com/images/I/51Xy2XNo2YL._SL160_.jpg", List(PriceAtTime(price = 1835, date = new Date)).asJava),
//    ProductItem("DK Eyewitness Travel Guide: China", "0756684307", "", "http://ecx.images-amazon.com/images/I/51Xy2XNo2YL._SL160_.jpg", List(PriceAtTime(price = 1835, date = new Date)).asJava),
//    ProductItem("Lonely Planet China (Travel Guide)", "1742201385", "", "http://ecx.images-amazon.com/images/I/51NK2%2B-q81L._SL160_.jpg", List(PriceAtTime(price = 2165, date = new Date)).asJava))

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

  def googleFuzzyMatch(url: String) =
    this.curl(url).select("span:matchesOwn(Showing results)").parents().get(1).children().first().children().get(1).getAllElements.first().text()

  def getEbayDeals = {
    val doc = this.curl("http://deals.ebay.com/feeds/rss")
    val entries = doc.select("entry").subList(0, 10).asScala
    val deals = for {entry <- entries } yield  {
      val title = entry.select("title").get(0).text()
      val link = entry.select("link").get(0).attr("href")
      val imgUrl = entry.select("img").get(0).attr("src")
      Deal(title, link, imgUrl)
    }
     deals
  }

  private def curl(url:String) = Jsoup.connect(url.replace(" ", "+")).userAgent("Mozilla").get()
  def getPriceDisplay(price:Int) = "$" + price.toFloat/100
}
