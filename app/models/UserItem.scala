package models

import org.codehaus.jackson.annotate.JsonProperty
import beans.BeanProperty
import java.util.Date
import org.apache.commons.lang3.StringUtils
import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.Id
import net.vz.mongodb.jackson.ObjectId

case class UserItem( @Id     @JsonProperty("_id") var id: String,
                 @BeanProperty @JsonProperty("date0") val userId: String,
                 @BeanProperty @JsonProperty("date1") val itemId: String,
                 @BeanProperty @JsonProperty("date2") val date: Date,
                 @BeanProperty @JsonProperty("date3") val priceOriginal: String,
                 @BeanProperty @JsonProperty("date4") val priceExpected: String
) {
  def getId = id
}


object UserItem {
  private lazy val db = MongoDB.collection("userItem", classOf[UserItem], classOf[String])

  def findAll(userId: String): List[UserItem] = {
    val cursor = db.find().is("userId", userId)
    var userItems :List[UserItem] = Nil
    while (cursor.hasNext()) {
      val userItem =   cursor.next()
      userItems = userItem :: userItems
    }
    userItems
  }

  def save(userItem:UserItem):UserItem={
    if (StringUtils.isBlank(userItem.id)){
      userItem.id = (new Date()).getTime.toString
    }
    db.save(userItem).getSavedObject
  }

  def delete(itemId: String) {
    val cursor = db.find().is("itemId", itemId)
    if (null != cursor && cursor.hasNext()) {
      val userItem =   cursor.next()
      db.remove(userItem)
    }
  }

}

