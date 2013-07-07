package models

import beans.BeanProperty
import org.codehaus.jackson.annotate.JsonProperty
import java.util.Date

class UserItem(@BeanProperty @JsonProperty var itemId: String,
               @BeanProperty @JsonProperty var orderDate: Date,
               @BeanProperty @JsonProperty var priceOriginal: Int,
               @BeanProperty @JsonProperty var priceExpected: Int
                ) {
  def this() = this(itemId = "", orderDate = null, priceOriginal = 0, priceExpected = 0)
}