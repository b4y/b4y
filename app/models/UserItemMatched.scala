package models

import scala.collection.JavaConverters._
import util.EmailUtil


case class UserItemMatched(user: User, item: ProductItem, priceExpected: Int, priceMatched: Int)

object UserItemMatched {
  def runPriceMatch(): List[UserItemMatched] = {
    var userItemsMatched: List[UserItemMatched] = Nil
    User.findAll().foreach(user => {
      user.userItems.asScala.foreach(userItem => {
        val productItem = ProductItem.load(userItem.itemId)
        val priceExpected = userItem.priceExpected
        val items = TestClient.itemLookup(productItem.asin)
        if (items.get(0).getOfferSummary.getLowestNewPrice != null) {
          val itemPrice = items.get(0).getOfferSummary.getLowestNewPrice.getAmount
          if (itemPrice.intValue() <= priceExpected) {
            val userItemMatched = UserItemMatched(user, productItem, priceExpected, itemPrice.intValue())
            userItemsMatched = userItemMatched :: userItemsMatched
            EmailUtil.sendPriceMatchEmail(userItemMatched)
          }
        }
      })
    })
    userItemsMatched
  }
}