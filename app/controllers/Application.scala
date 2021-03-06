package controllers

import _root_.util.{EmailUtil, BUtil}
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import templates.Html
import scala.collection.JavaConverters._
import java.util
import util.Date
import models.ProductItem.PriceAtTime
import play.api.libs.json.Json
import org.codehaus.jackson.map.ObjectMapper

object Application extends Controller {
  val SessionNameUserId = "UserId"
  def index = Action { implicit request => {
    val userId = session.get(SessionNameUserId)
    var firstName = "";
    if(userId.isDefined){
    	var user = BUtil.getUser(session);
    	firstName = user.firstName;
    }
    Ok(views.html.storeFront(isLoggedIn = userId.isDefined, searchIndices = searchIndices, firstName= firstName))
  } }

  def tasks = Action {
    Ok(views.html.index(Task.all(), taskForm))
  }

  def newTask = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(Task.all(), errors)),
      label => {
        Task.create(label)
        Redirect(routes.Application.tasks())
      }
    )
  }

  def deleteTask(id: String) = Action {
    Task.delete(id)
    Redirect(routes.Application.tasks())
  }

  val taskForm = Form(
    "label" -> nonEmptyText
  )

  val prodSearchForm = Form(
    "prodSearchWord" -> nonEmptyText
  )

  val testClient = new TestClient
  val searchIndices = List("All", "Apparel", "Appliances", "ArtsAndCrafts", "Automotive", "Baby", "Beauty", "Blended", "Books", "Classical", "Collectibles", "DigitalMusic", "Grocery", "DVD", "Electronics", "HealthPersonalCare", "HomeGarden", "Industrial", "Jewelry", "KindleStore", "Kitchen", "LawnGarden", "Magazines", "Marketplace", "Merchants", "Miscellaneous", "MobileApps", "MP3Downloads", "Music", "MusicalInstruments", "MusicTracks", "OfficeProducts", "OutdoorLiving", "PCHardware", "PetSupplies", "Photo", "Shoes", "Software", "SportingGoods", "Tools", "Toys", "UnboxVideo", "VHS", "Video", "VideoGames", "Watches", "Wireless", "WirelessAccessories")
  def prodSearchResult = Action { implicit request =>
    prodSearchForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(Task.all(), errors)),
      prodSearchWord => {
        val items = {
          val searchIndex = prodSearchForm.bindFromRequest.data.get("searchIndex").get
          val itemsAws = testClient.runSearch(prodSearchWord, searchIndex)
          val itemsFirstSearch = itemsAws.asScala.map(ProductItem.convertProductItemFromAwsItem(_)).toList.filter(_.priceHistory.get(0).price > 0)
          if (itemsFirstSearch.size > 0)
            itemsFirstSearch
          else {
            val prodSearchWordRevised = BUtil.googleFuzzyMatch("https://www.google.com/search?q=" + prodSearchWord)
            val itemsAws = testClient.runSearch(prodSearchWordRevised, searchIndex)
            itemsAws.asScala.map(ProductItem.convertProductItemFromAwsItem(_)).toList.filter(_.priceHistory.get(0).price > 0)
          }
        }

        //todo: if user login, and item already in cart, replace button text from 'order this item' to 'item already in cart' and disable button click
        val mapper = new ObjectMapper()
        if (BUtil.isTest){
        	val a = mapper.writeValueAsString(items.asJava)
          	val prodSearchresult = Json.toJson(
          		Map(
          			"prodList" -> a
          		)
          	)
            System.out.println("debug 777777")
          	Ok(prodSearchresult)
        }else{
          val a = mapper.writeValueAsString(items.asJava)
          val prodSearchresult = Json.toJson(
            Map(
              "prodList" -> a
            )
          )
          System.out.println("debug 888")
          Ok(prodSearchresult)
        }
      }
    )
  }

  def selectItem(name: String, asin:String, detailPageURL:String, price: Int,  img: String) = Action {
    val isItemAlreadyOrdered = ProductItem.isFieldValueInDb(ProductItem.DbFieldAsin, asin)
    if (isItemAlreadyOrdered) {
      val item = ProductItem.findByField(ProductItem.DbFieldAsin, asin)
      val latestPriceAtTime =  item.priceHistory.get(0)
      if (latestPriceAtTime.price != price || latestPriceAtTime.date.getTime +  OneDay <  (new Date).getTime){
        val newPRice = PriceAtTime(price, "$" + price.toFloat/100, new Date())
        item.priceHistory.add(0, newPRice)
        ProductItem.save(item)
      }
    }
    if (isItemAlreadyOrdered)
      Redirect(routes.Application.items())
    else
      Ok(views.html.selectItem(name, asin, detailPageURL, price, img, itemOrderForm))
  }

  val OneDay = 24 * 60 * 60 * 1000
  def saveUserItem() = Action {implicit request =>{
    val data = itemOrderForm.bindFromRequest.data
    val (name, asin, detailPageURL, img, priceOriginal, priceExpected) = (
      data.get("name").get,
      data.get("asin").get,
      data.get("detailPageURL").get,
      data.get("img").get,
      data.get("price").get,
      data.get("newPrice").get)
    val item = ProductItem(name, asin, detailPageURL, img, List(PriceAtTime(price = priceOriginal.toInt, priceDisplay= BUtil.getPriceDisplay(priceOriginal.toInt), date = new Date)).asJava)
    ProductItem.save(item)
    val userItem = new UserItem(item.id, new Date, priceOriginal.toInt, (priceExpected.toFloat * 100).toInt)
    val user = BUtil.getUser(session)
    user.addItem(userItem)
    User.save(user)

    val result = Json.toJson(
      Map(
        "success" -> Json.toJson("yes")
      )
    )
    Ok(result)
  } }

  val itemOrderForm = Form(tuple(
    "name" -> nonEmptyText,
    "asin" -> nonEmptyText,
    "detailPageURL" -> nonEmptyText,
    "img" -> nonEmptyText,
    "priceOriginal" -> nonEmptyText,
    "newPrice" -> nonEmptyText)
  )

  def items = Action {implicit request =>{
	    val user:User = try {
	      BUtil.getUser(session)
	    } catch {
	      case ioe: IllegalStateException =>
	        Redirect(routes.Application.index())
	      null
	    }
	    if (null == user){
	      Redirect(routes.Application.signUp()).withNewSession
	    }
	    else {
	        val mapper = new ObjectMapper()
        	val a = mapper.writeValueAsString((new UserWithProductItems(user)).userItemsWithProductItem)
          	val itemListResult = Json.toJson(
          		Map(
          		    "success"->"yes",
          			"itemList" -> a
          		)
          	)
          	Ok(itemListResult)	        
	    }
  	}  
  }


  def deleteItem(id: String) = Action {implicit request =>{
	    val user = BUtil.getUser(session)
    user.userItems = user.userItems.asScala.filterNot(_.itemId.equalsIgnoreCase(id)).toList.asJava
    User.save(user)
	    val result = Json.toJson(
	      Map(
	        "success" -> Json.toJson("yes")
	      )
	    )
	    Ok(result)
  	} 
  }



  def buyItem(id: String, qty:Int = 1) = Action {
    val item = ProductItem.load(id)
    val purchaseUrl = testClient.addToCart(item.asin, qty)
    Redirect(purchaseUrl)
  }

  val signUpForm = Form(tuple(
    "firstName" -> nonEmptyText,
    "lastName" -> nonEmptyText,
    "email" -> nonEmptyText,
    "password" -> nonEmptyText)
  )
  def signUp() = Action {implicit request =>{
    signUpForm.discardingErrors
    val data = signUpForm.bindFromRequest.data
    val (firstName, lastName, email, password) = (
      data.get("firstName").get,
      data.get("lastName").get,
      data.get("email").get,
      data.get("password").get)

    if (User.isFieldValueInDb(User.DbFieldEmail, email)){
       Ok(Json.toJson(
	        Map(
	          "error" -> Json.toJson("Email " + email + " already registered")
	        )
    	  )
       )
    }
    else {
      val passwordEncrypted = BUtil.encrypt(password)
      val user = new User(firstName, lastName, email, passwordEncrypted, User.AccountStatusPending,
                          new util.ArrayList[UserItem]())
      val userSaved = User.save(user)
      EmailUtil.sendSignUpEmail(user.email, user.firstName, user.lastName, userSaved.id)
      Ok(Json.toJson(
            Map("success" -> Json.toJson("yes") )
        )
      )
    }
   }
 }

  val resetPasswordRequestForm = Form("email" -> nonEmptyText)
  def resetPasswordRequest() = Action {
    implicit request => {
      resetPasswordRequestForm.discardingErrors
      val data = resetPasswordRequestForm.bindFromRequest.data
      val email = data.get("email").get
      val user = User.findByField(User.DbFieldEmail, email)
      if (null == user) Ok(Json.toJson(Map("error" -> Json.toJson("Email " + email + " not found in System"))))
      else {
        val newPassword = if (BUtil.SuperUserJim.equalsIgnoreCase(user.email)) "hhhhhh" else user.password.substring(0, 6)
        User.resetPassword(user, newPassword)
        EmailUtil.sendResetPasswordEmail(user.email, user.firstName, user.lastName, user.id, newPassword)
        Ok(Json.toJson(Map("success" -> Json.toJson("yes"))))
      }
    }
  }

  def resetPassword(email: String, newPassword: String) = Action {
    implicit request => {
      val user = User.findByField(User.DbFieldEmail, email)
      if (null == user) Ok(Json.toJson(Map("error" -> Json.toJson("Email " + email + " already registered"))))
      else {
        User.resetPassword(user, newPassword)
        Ok(Json.toJson(Map("success" -> Json.toJson("yes"))))
      }
    }
  }

  def signIn() = Action {implicit request =>{
    signUpForm.discardingErrors
    val data = signUpForm.bindFromRequest.data
    val (email, password) = (
      data.get("email").get,
      data.get("password").get)
    if (!User.isFieldValueInDb(User.DbFieldEmail, email)){
        val result = Json.toJson(
               Map("error" -> Json.toJson("Email " + email + " is not registered"))
        )
        Ok(result);          
    }
    else {
      val user = User.findByField(User.DbFieldEmail, email)
      val passwordEncrypted = BUtil.encrypt(password)
      if (user.password.equalsIgnoreCase(passwordEncrypted) && user.accountStatus == User.AccountStatusActive){
        val result = Json.toJson(
               Map("success" -> Json.toJson("yes"),
                   "firstName" -> Json.toJson(user.firstName) )
        )
        Ok(result).withSession(session + (SessionNameUserId -> user.id))
      }
      else{
        val result = Json.toJson(
               Map("error" -> Json.toJson("invalid password for email  " + email))
        )
        Ok(result);          
      }      
    }
  }
  }

  def signOut = Action { implicit request => {
       Redirect(routes.Application.index()).withNewSession
    }

  }

  def activateAccount(userId: String) = Action {implicit request =>{
    User.activateAccount(userId)
    Ok(views.html.acctivateAccount())
  } }

  def admin = Action {implicit request => {
    def isAdminUser :Boolean = {
      val AdminUsers = List("jigang_hao@hotmail.com")
      val userIdOption = session.get(SessionNameUserId)
      if (userIdOption.isEmpty)
        return false
      val userId = userIdOption.get
      val user = User.load(userId)
      val result = AdminUsers.contains(user.email)
      result
    }

    if (isAdminUser)
      Ok(views.html.admin())
    else
      Ok(views.html.adminUnauthorized())
  } }


  def adminUserList = Action {implicit request =>{
    val users = User.findAll()
    Ok(views.html.adminUserList(users))
  } }

  def adminDeleteUser(id: String) = Action {implicit request =>{
    User.delete(id)
    val users = User.findAll()
    Ok(views.html.adminUserList(users))

  } }

  def adminUserItemList(userId: String) = Action {implicit request =>{
    val user = BUtil.getUser(userId)
//    val userItems = (Option(user.userItems) getOrElse (new util.ArrayList[ProductItem]())).asScala.toList
    Ok(views.html.items((new UserWithProductItems(user)).userItemsWithProductItem.asScala.toList))
  } }

  def adminItemList = Action {implicit request =>{
    val items = ProductItem.findAll()
    Ok(views.html.adminItemList(items))
  } }

  def runPriceMatch = Action {implicit request =>{
    val items = UserItemMatched.runPriceMatch()
    if (items.isEmpty)
      Ok(views.html.adminItemNoMatch(items))
    else
      Ok(views.html.adminItemMatchedList(items))
  } }
  
  def myProfile() = Action {
    implicit request => {
	    val userId = session.get(SessionNameUserId)
	    var firstName = "";
	    if(userId.isDefined){
	    	var user = BUtil.getUser(session);
	    	firstName = user.firstName;
	    }
	    Ok(views.html.mgr.myProfile(isLoggedIn = userId.isDefined, firstName= firstName, ""))
    }
  }
  
  val resetPassForm = Form(tuple(
    "currentPass" -> nonEmptyText,
    "newPass" -> nonEmptyText)
  )  
  def setNewPass() = Action {
    implicit request => {
    	resetPassForm.discardingErrors
    	val data = resetPassForm.bindFromRequest.data
	    val userId = session.get(SessionNameUserId)
	    var firstName = "";
	    if(userId.isDefined){
	    	var user = BUtil.getUser(session);
	    	firstName = user.firstName;
	    	
	        val passwordEncrypted = BUtil.encrypt(data.get("currentPass").get)

	        if (!user.password.equalsIgnoreCase(passwordEncrypted)){
	          Ok(Json.toJson(
	               Map("success" -> Json.toJson("no"),
	                   "error" -> Json.toJson("Current password is not correct") )
	          ))
	        }else{
	          user.password =  BUtil.encrypt(data.get("newPass").get);
	          User.save(user)
	          Ok(Json.toJson(
	               Map("success" -> Json.toJson("yes"))
	          ))
	        }
	    }else{
	        Ok(views.html.storeFront(isLoggedIn = userId.isDefined, searchIndices = searchIndices, firstName= firstName))
	    }
    }
  }  
}