package models

import beans.BeanProperty
import java.util.Date
import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.Id
import models.User.UserItem
import scala.collection.JavaConverters._
import java.util

class User(   @Id var id: String,
                   @BeanProperty @JsonProperty("date5") var firstName: String,
                   @BeanProperty @JsonProperty("date4") var lastName: String,
                   @BeanProperty @JsonProperty("date1") var email: String,
                   @BeanProperty @JsonProperty("date2") var password: String,
                   @BeanProperty @JsonProperty("date3") var userItems: java.util.ArrayList[UserItem]
) {
  def this() = this((new Date()).getTime.toString, "", "", "", "", new java.util.ArrayList[UserItem]())
  @Id  def getId = id

  def addItem(userItem: UserItem){
    if (null == userItems)
      userItems = new java.util.ArrayList[UserItem]()
    userItems.add(userItem)
  }
}

object User {
  private lazy val db = MongoDB.collection("user", classOf[User], classOf[String])

  def all(): List[User] = {
    val cursor = db.find()
    var users :List[User] = Nil
    while (cursor.hasNext) {
      val user =   cursor.next()
      users = user :: users
    }
    users
  }

  def save(user:User):User={
//    if (StringUtils.isBlank(user.id)){
//      user.id = (new Date()).getTime.toString
//    }
    db.save(user).getSavedObject
  }

  def delete(id: String) = db.removeById(id)
  def load(id: String) = db.findOneById(id)

  def emailExisted(email: String):Boolean = {
    val cursor = db.find().is("email", email)
    null != cursor && cursor.hasNext
  }

  def findByEmail(email: String):User = {
    val cursor = db.find().is("email", email)
    if (null == cursor || !cursor.hasNext)
      null
    else
      cursor.next()
  }

   class UserItem( @BeanProperty @JsonProperty("date1") var itemId: String,
                       @BeanProperty @JsonProperty("date2") var orderDate: Date,
                       @BeanProperty @JsonProperty("date3") var priceOriginal: Int,
                       @BeanProperty @JsonProperty("date4") var priceExpected: Int
  ){
     def this() = this("", null, 0, 0)

   }

}

class UserWithProductItems(@BeanProperty @JsonProperty("date6") var userItemsWithProductItem: java.util.ArrayList[UserItemWithProductItem]) extends User {
  def this() =  this(new  java.util.ArrayList[UserItemWithProductItem]())

  def this(user: User) = {
    this()
    this.id = user.id
    this.firstName = user.firstName
    this.lastName = user.lastName
    this.email = user.email
    this.password = user.password
    this.userItems = user.userItems
     val aaa = user.userItems.asScala.map(userItem => {
      val userItemWithProductItem = new UserItemWithProductItem(ProductItem.load(userItem.itemId))
       userItemWithProductItem.itemId = userItem.itemId
       userItemWithProductItem.orderDate = userItem.orderDate
       userItemWithProductItem.priceOriginal = userItem.priceOriginal
       userItemWithProductItem.priceExpected = userItem.priceExpected
       userItemWithProductItem
    }).asJava
    this.userItemsWithProductItem = new util.ArrayList[UserItemWithProductItem]()
    this.userItemsWithProductItem.addAll(aaa)
  }
}


 class UserItemWithProductItem(@BeanProperty @JsonProperty("date6") var item: ProductItem) extends UserItem
