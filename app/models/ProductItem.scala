package models


import _root_.util.{DbId, DbUtil}
import scala.collection.JavaConverters._
import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.JacksonDBCollection
import awsClient4.Item
import java.util.Date
import models.ProductItem.PriceAtTime
import java.util
import beans.BeanProperty
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray
import scala.util.parsing.json.JSONArray
import org.codehaus.jackson.map.ObjectMapper;

case class ProductItem(
            @BeanProperty @JsonProperty name: String,
            @BeanProperty @JsonProperty asin: String,
            @BeanProperty @JsonProperty detailPageURL: String,
            @BeanProperty @JsonProperty img: String,
            @BeanProperty @JsonProperty priceHistory: java.util.List[PriceAtTime]
                  ) extends DbId {
  def this() = this("", "", "","", new util.ArrayList[PriceAtTime]())
 /* 
  def getJson(): JsValue ={
    
    return Json.toJson(
       Map(
           "name"-> Json.toJson(this.getName),
           "asin"-> Json.toJson(this.getAsin),
           "detailPageURL"-> Json.toJson(this.getDetailPageURL),
           "img"-> Json.toJson(this.getImg)
           //"priceHistory"-> Json.toJson(this.getPriceHistory)
       )
    )
  }  
  *
  */
}

object ProductItem {
  val DbFieldAsin = "asin"
  private lazy val db = MongoDB.collection("item", classOf[ProductItem], classOf[String])
  private val dbDelegate = db.asInstanceOf[JacksonDBCollection[AnyRef,  String]]

  def load(id: String) = DbUtil.load(dbDelegate, id).asInstanceOf[ProductItem]
  def findAll() = DbUtil.findAll(dbDelegate).asInstanceOf[List[ProductItem]]
  def findByField(field: String, value: String) = DbUtil.findOneByField(dbDelegate, field, value).asInstanceOf[ProductItem]
  def isFieldValueInDb(field: String, value: String) = DbUtil.isFieldValueInDb(dbDelegate, field, value)
  def save(productItem: ProductItem) = DbUtil.save(dbDelegate, productItem).asInstanceOf[ProductItem]
  def delete(id: String) = db.removeById(id)

  def convertProductItemFromAwsItem(item: Item): ProductItem = {
    val price: Int = try {
      item.getOffers.getOffer.get(0).getOfferListing.get(0).getPrice.getAmount.intValue()
    } catch {
      case _: Throwable => 0
    }
    val priceHistory = new util.ArrayList[PriceAtTime]()
    priceHistory.add(PriceAtTime(price = price, priceDisplay = "$" + price.toFloat/100, date = new Date))
    //todo: use real 'no-image' image
    val imgUrl = if (null == item.getMediumImage)
      "http://ecx.images-amazon.com/images/I/41nPFVINbhL._SL160_.jpg"
      else item.getMediumImage.getURL
    ProductItem(item.getItemAttributes.getTitle,
      item.getASIN,
      item.getDetailPageURL,
      imgUrl,
      priceHistory
    )
  }

  case class PriceAtTime( @BeanProperty @JsonProperty("date1")price: Int,
                          @BeanProperty @JsonProperty("date3")priceDisplay: String,
                         @BeanProperty @JsonProperty("date2")date: Date)
  }

case class ProductItemAndIfOrderedPair(item: ProductItem, isAlreadyOrdered:Boolean)
