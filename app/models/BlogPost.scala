package models
import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import javax.persistence.Id
import play.modules.mongodb.jackson.MongoDB
import java.util.Date
import play.api.Play.current
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

class BlogPost(@JsonProperty("_id") val id: String,
               @BeanProperty @JsonProperty("date") val date: Date,
               @BeanProperty @JsonProperty("title") val title: String,
               @BeanProperty @JsonProperty("author") val author: String,
               @BeanProperty @JsonProperty("content") val content: String) {
  def getId = id
}

object BlogPost {
  private lazy val db = MongoDB.collection("blogposts", classOf[BlogPost], classOf[String])

  def save(blogPost: BlogPost) { db.save(blogPost) }
  def findById(id: String) = Option(db.findOneById(id))
  def findByAuthor(author: String) = db.find().is("author", author)
}