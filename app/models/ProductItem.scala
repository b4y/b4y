package models


import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.Id
import net.vz.mongodb.jackson.ObjectId
import awsClient4.{OfferSummary, Item}
import java.util.Date

case class ProductItem( @Id  @JsonProperty("_id") val id: String,
            @BeanProperty @JsonProperty("date") val name: String,
            @BeanProperty @JsonProperty("date4") val asin: String,
            @BeanProperty @JsonProperty("date1") val img: String,
            @BeanProperty @JsonProperty("date2") val priceOriginal: String,
            @BeanProperty @JsonProperty("date3") val priceExpected: String
                  ) {
  def getId = id
}


object ProductItem {
  private lazy val db = MongoDB.collection("item", classOf[ProductItem], classOf[String])

  def all(): List[ProductItem] = {
    val cursor = db.find()
    var items :List[ProductItem] = Nil
    while (cursor.hasNext()) {
      val item =   cursor.next()
      items = item :: items
    }
    items
  }

  def findByItemIds(itemIds: List[String]): List[ProductItem] = {
    itemIds.map(findProductItemById(_))
  }

  def create(name: String, asin: String, img:String, priceOriginal:String, priceExpected:String):ProductItem = {
    val item = new ProductItem("", name, asin, img, priceOriginal, priceExpected)
    item
  }

  def save(name: String, asin: String, img:String, priceOriginal:String, priceExpected:String):ProductItem ={
    val idCount: Long = (new Date()).getTime
    val item = new ProductItem(idCount.toString, name, asin, img, priceOriginal, priceExpected)
    db.save(item).getSavedObject
  }

  def delete(id: String) {
    val cursor = db.find().is("id", id)
    if (null != cursor && cursor.hasNext()) {
      val item =   cursor.next()
      db.remove(item)

    }
  }

  def findProductItemById(id: String):ProductItem = {
    val cursor = db.find().is("id", id)
    if (null != cursor && cursor.hasNext()) {
      val item =   cursor.next()
      item
    } else
    null
  }

  def convertProductItemFromAwsItem(item:Item) :ProductItem = {
    val price = Option(item.getOfferSummary) match {
      case None => "N/A"
      case Some(summary: OfferSummary) => summary.getLowestNewPrice.getFormattedPrice
    }
//    val offerListingId = item.getOffers.getOffer.get(0).getOfferListing.get(0).getOfferListingId
    ProductItem.create(item.getItemAttributes.getTitle,
      item.getASIN,
      item.getMediumImage.getURL,
      price,
      null)
  }


  }
