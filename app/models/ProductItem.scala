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
import beans.BeanProperty

case class ProductItem(
            @BeanProperty @JsonProperty name: String,
            @BeanProperty @JsonProperty asin: String,
            @BeanProperty @JsonProperty detailPageURL: String,
            @BeanProperty @JsonProperty img: String,
            @BeanProperty @JsonProperty priceHistory: java.util.List[PriceAtTime]
                  ) extends DbId {
  def this() = this("", "", "","", new util.ArrayList[PriceAtTime]())
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
    val (isAvailable, price: Int) = try {
      (true, item.getOffers.getOffer.get(0).getOfferListing.get(0).getPrice.getAmount.intValue())
    } catch {
      case _: Throwable => (false, 0)
    }
    val priceHistory = new util.ArrayList[PriceAtTime]()
    priceHistory.add(PriceAtTime(available = isAvailable, price = price, date = new Date))
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


  case class PriceAtTime(@BeanProperty @JsonProperty("date0") available: Boolean,
                         @BeanProperty @JsonProperty("date1")price: Int,
                         @BeanProperty @JsonProperty("date2")date: Date)
  }
