package util

import net.vz.mongodb.jackson.{Id, JacksonDBCollection}
import scala.Predef._
import org.apache.commons.lang3.StringUtils
import java.util.Date

object DbUtil {
  def delete(db: JacksonDBCollection[AnyRef,  String], id: String) = db.removeById(id)
  def load(db: JacksonDBCollection[AnyRef,  String], id: String) = db.findOneById(id)
  //  def findByItemIds(itemIds: List[String]): List[ProductItem] = itemIds.map(load(_))

  def isFieldValueInDb(db: JacksonDBCollection[AnyRef,  String], field: String, value: String): Boolean = {
    val cursor = db.find().is(field, value)
    null != cursor && cursor.hasNext
  }

  def findOneByField(db: JacksonDBCollection[AnyRef,  String], field: String, value: String): AnyRef = {
    val cursor = db.find().is(field, value)
    if (null != cursor && cursor.hasNext)
      cursor.next
    else
      null
  }

  def findAll(db: JacksonDBCollection[AnyRef,  String]): List[AnyRef] = {
    val cursor = db.find()
    var dbEntries :List[AnyRef] = Nil
    while (cursor.hasNext) {
      val dbEntry =   cursor.next()
      dbEntries = dbEntry :: dbEntries
    }
    dbEntries
  }

  def save(db: JacksonDBCollection[AnyRef,  String], entityWithId: DbId):AnyRef ={
    if (StringUtils.isBlank(entityWithId.id)){
      entityWithId.id = (new Date()).getTime.toString
    }
    db.save(entityWithId).getSavedObject
  }
}

trait DbId{
  @Id  var id: String = _
  @Id def getId = id

}