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

case class User( @Id     @JsonProperty("_id") var id: String,
                   @BeanProperty @JsonProperty("date") val firstName: String,
                   @BeanProperty @JsonProperty("date4") val lastName: String,
                   @BeanProperty @JsonProperty("date1") val email: String,
                   @BeanProperty @JsonProperty("date2") val password: String
                   ) {
  def getId = id
}


object User {
  private lazy val db = MongoDB.collection("user", classOf[User], classOf[String])

  def all(): List[User] = {
    val cursor = db.find()
    var users :List[User] = Nil
    while (cursor.hasNext()) {
      val user =   cursor.next()
      users = user :: users
    }
    users
  }

  def save(user:User){
    if (StringUtils.isBlank(user.id)){
      user.id = (new Date()).getTime.toString
    }
    db.save(user)
  }

  def delete(id: String) {
    val cursor = db.find().is("id", id)
    if (null != cursor && cursor.hasNext()) {
      val user =   cursor.next()
      db.remove(user)
    }
  }

  def load(id: String):User = {
    val cursor = db.find().is("id", id)
    if (null != cursor && cursor.hasNext()) {
      val user =   cursor.next()
      user
    } else
      null
  }

  def emailExisted(email: String):Boolean = {
    val cursor = db.find().is("email", email)
    null != cursor && cursor.hasNext()
  }

  def findByEmail(email: String):User = {
    val cursor = db.find().is("email", email)
    if (null == cursor || !cursor.hasNext())
      null
    else
      cursor.next()
  }
}

