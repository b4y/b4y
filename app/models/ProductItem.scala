package models


import _root_.util.{DbId, DbUtil}
import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.JacksonDBCollection
import awsClient4.Item
import java.util.Date
import models.ProductItem.PriceAtTime
import java.util
import org.apache.commons.lang3.StringUtils
import beans.BeanProperty

case class ProductItem(
            @BeanProperty @JsonProperty name: String,
            @BeanProperty @JsonProperty asin: String,
            @BeanProperty @JsonProperty img: String,
            @BeanProperty @JsonProperty priceHistory: java.util.List[PriceAtTime]
                  ) extends DbId {
  def this() = this("", "", "", new util.ArrayList[PriceAtTime]())
}

object ProductItem {
  val DbFieldAsin = "asin"
  private lazy val db = MongoDB.collection("item", classOf[ProductItem], classOf[String])
  private val dbDelegate = db.asInstanceOf[JacksonDBCollection[AnyRef,  String]]

  def findAll() = DbUtil.findAll(dbDelegate).asInstanceOf[List[ProductItem]]
//  def findAll(): List[ProductItem] = {
//    val cursor = db.find()
//    var items :List[ProductItem] = Nil
//    while (cursor.hasNext) {
//      val item =   cursor.next()
//      items = item :: items
//    }
//    items
//  }
  def save(productItem: ProductItem) = DbUtil.save(dbDelegate, productItem).asInstanceOf[ProductItem]
//  def save(productItem: ProductItem):ProductItem ={
//    if (StringUtils.isBlank(productItem.id)){
//      productItem.id = (new Date()).getTime.toString
//    }
//    db.save(productItem).getSavedObject
//  }

  def delete(id: String) = db.removeById(id)
  def load(id: String) = DbUtil.load(dbDelegate, id).asInstanceOf[ProductItem]
  def isFieldValueInDb(field: String, value: String) = DbUtil.isFieldValueInDb(dbDelegate, field, value)
  def findByField(field: String, value: String) = DbUtil.findOneByField(dbDelegate, field, value).asInstanceOf[ProductItem]

  def convertProductItemFromAwsItem(item: Item): ProductItem = {
    val (isAvailable, price: Int) = try {
      (true, item.getOffers.getOffer.get(0).getOfferListing.get(0).getPrice.getAmount.intValue())
    } catch {
      case _: Throwable => (false, 0)
    }
    val priceHistory = new util.ArrayList[PriceAtTime]()
    priceHistory.add(PriceAtTime(available = isAvailable, price = price, date = new Date))
    ProductItem(item.getItemAttributes.getTitle,
      item.getASIN,
      item.getMediumImage.getURL,
      priceHistory
    )
  }


  case class PriceAtTime(@BeanProperty @JsonProperty("date0") available: Boolean,
                         @BeanProperty @JsonProperty("date1")price: Int,
                         @BeanProperty @JsonProperty("date2")date: Date)
  }
