package models

/**
 * Created with IntelliJ IDEA.
 * User: jimhao
 * Date: 29/04/2013
 * Time: 21:59
 * To change this template use File | Settings | File Templates.
 */
import play.api.Play.current
import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import play.modules.mongodb.jackson.MongoDB
import net.vz.mongodb.jackson.Id
import net.vz.mongodb.jackson.ObjectId




class Task( @Id
            @ObjectId @JsonProperty("_id") val id: String,
                @BeanProperty @JsonProperty("date") val label: String) {
  def getId = id
}


object Task {
  private lazy val db = MongoDB.collection("tasks", classOf[Task], classOf[String])
  var idCount: Long = 0


  def all(): List[Task] = {
    val cursor = db.find()
    var tasks :List[Task] = Nil
    while (cursor.hasNext()) {
      val task =   cursor.next()
      tasks = task :: tasks
    }
    tasks
  }

  def create(label: String) {
    idCount += 1
    val task = new Task(idCount.toString, label)
    db.save(task)
  }

  def delete(id: String) {
    val cursor = db.find().is("id", id)
    if (null != cursor && cursor.hasNext()) {
      val task =   cursor.next()
            db.remove(task)

    }
  }
}