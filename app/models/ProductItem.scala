package models


import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.Id
import awsClient4.Item
import java.util.Date
import models.ProductItem.PriceAtTime
import java.util
import org.apache.commons.lang3.StringUtils
import beans.BeanProperty

case class ProductItem( @Id  var id: String,
            @BeanProperty @JsonProperty("date0") name: String,
            @BeanProperty @JsonProperty("date1") asin: String,
            @BeanProperty @JsonProperty("date2") img: String,
            @BeanProperty @JsonProperty("date3") priceHistory: java.util.List[PriceAtTime]
                  ) {
  def this() = this("", "", "", "", new util.ArrayList[PriceAtTime]())
  @Id def getId = id
}


object ProductItem {
  val DbFieldAsin = "asin"
  private lazy val db = MongoDB.collection("item", classOf[ProductItem], classOf[String])

  def all(): List[ProductItem] = {
    val cursor = db.find()
    var items :List[ProductItem] = Nil
    while (cursor.hasNext) {
      val item =   cursor.next()
      items = item :: items
    }
    items
  }
  def save(productItem: ProductItem):ProductItem ={
    if (StringUtils.isBlank(productItem.id)){
      productItem.id = (new Date()).getTime.toString
    }
    db.save(productItem).getSavedObject
  }

  def delete(id: String) = db.removeById(id)
  def load(id: String) = db.findOneById(id)
  //  def findByItemIds(itemIds: List[String]): List[ProductItem] = itemIds.map(load(_))

  def isFieldValueInDb(field: String, value: String): Boolean = {
    val cursor = db.find().is(field, value)
    null != cursor && cursor.hasNext
  }

  def findByFieldValue(field: String, value: String): ProductItem = {
    val cursor = db.find().is(field, value)
    if (null != cursor && cursor.hasNext)
      cursor.next
    else
      null
  }

  def convertProductItemFromAwsItem(item:Item) :ProductItem = {
//    val (available: Boolean, price : Long) = Option(item.getOfferSummary) match {
//      case None => (false, 0)
//      case Some(summary: OfferSummary) => (true, summary.getLowestNewPrice.getAmount)
//    }
  val (isAvailable, price:Int) = try {
    (true, item.getOffers.getOffer.get(0).getOfferListing.get(0).getPrice.getAmount.intValue())
  } catch {
    case  _ : Throwable=> (false, 0)
  }
  val priceHistory = new util.ArrayList[PriceAtTime]()
  priceHistory.add(PriceAtTime(available = isAvailable, price = price, date = new Date))
  ProductItem("",
    item.getItemAttributes.getTitle,
    item.getASIN,
    item.getMediumImage.getURL,
    priceHistory
  )
  }


  case class PriceAtTime(@BeanProperty @JsonProperty("date0") available: Boolean,
                         @BeanProperty @JsonProperty("date1")price: Int,
                         @BeanProperty @JsonProperty("date2")date: Date)
  }
