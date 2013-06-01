package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import templates.Html
import scala.collection.JavaConverters._
import java.util


object Application extends Controller {
  val SessionNameUserId = "UserId"
  def index = Action { implicit request => {
    val userId = session.get(SessionNameUserId)
    if (userId.isDefined)
          Redirect(routes.Application.items())
    else
      Redirect(routes.Application.signUp())
  }
//    Ok(views.html.index("Your new application is ready."))
  }

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



  def test = Action {
    Status(488)("Strange response type")
  }

  def main = Action {
    Ok(views.html.main(title = "jim")(content = Html("<h1> a test</h1>")))
  }

  def prodSearch = Action {
    Ok(views.html.productSearch("ProdSearch", searchIndices, prodSearchForm))
  }
  val prodSearchForm = Form(
    "prodSearchWord" -> nonEmptyText
  )

  val testClient = new TestClient
  var searchIndices = List("All", "Apparel", "Appliances", "ArtsAndCrafts", "Automotive", "Baby", "Beauty", "Blended", "Books", "Classical", "Collectibles", "DigitalMusic", "Grocery", "DVD", "Electronics", "HealthPersonalCare", "HomeGarden", "Industrial", "Jewelry", "KindleStore", "Kitchen", "LawnGarden", "Magazines", "Marketplace", "Merchants", "Miscellaneous", "MobileApps", "MP3Downloads", "Music", "MusicalInstruments", "MusicTracks", "OfficeProducts", "OutdoorLiving", "PCHardware", "PetSupplies", "Photo", "Shoes", "Software", "SportingGoods", "Tools", "Toys", "UnboxVideo", "VHS", "Video", "VideoGames", "Watches", "Wireless", "WirelessAccessories")
  def prodSearchResult = Action { implicit request =>
    prodSearchForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(Task.all(), errors)),
      prodSearchWord => {
        val items = if (BUtil.isTest)
          List(ProductItem("", "On China", "0143121316", "http://ecx.images-amazon.com/images/I/41nPFVINbhL._SL160_.jpg", "$9.50", null),
            ProductItem("", "DK Eyewitness Travel Guide: China", "0756684307", "http://ecx.images-amazon.com/images/I/51Xy2XNo2YL._SL160_.jpg", "$18.35", null),
            ProductItem("", "Lonely Planet China (Travel Guide)", "1742201385", "http://ecx.images-amazon.com/images/I/51NK2%2B-q81L._SL160_.jpg", "$21.65", null))
        else {
          val searchIndex = prodSearchForm.bindFromRequest.data.get("searchIndex").get
          val itemsAws = testClient.runSearch(prodSearchWord, searchIndex);
          itemsAws.asScala.map(ProductItem.convertProductItemFromAwsItem(_)).toList
        }
        Ok(views.html.productSearchResult("prodlist", items))
      }
    )
  }

  def selectItem(name: String, asin:String, price: String,  img: String) = Action {
    Ok(views.html.selectItem(name, asin, price, img, itemOrderForm))
  }

  def saveUserItem() = Action {implicit request =>{
    val data = itemOrderForm.bindFromRequest.data
    val (name, asin, img, currentPrice, newPrice) = (
      data.get("name").get,
      data.get("asin").get,
      data.get("img").get,
      data.get("price").get,
      data.get("newPrice").get)
    val itemSaved = ProductItem.save(name, asin, img, currentPrice, newPrice)
    val user = BUtil.getUser(session)
    user.addItem(itemSaved)
    User.save(user)
    Redirect(routes.Application.items())
  } }

  val itemOrderForm = Form(tuple(
    "name" -> nonEmptyText,
    "asin" -> nonEmptyText,
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
      val items = (Option(user.items) getOrElse (new util.ArrayList[ProductItem]())).asScala.toList
      Ok(views.html.items(items))
    }
  }  }

  def deleteItem(id: String) = Action {implicit request =>{
    val user = BUtil.getUser(session)
      val aaa = user.items.asScala.filterNot(_.id.equalsIgnoreCase(id)).toList.asJava
    user.items =new util.ArrayList[ProductItem](aaa)
      User.save(user)
    Redirect(routes.Application.items())
  } }



  def buyItem(id: String, qty:Int = 1) = Action {
    val item = ProductItem.findProductItemById(id)
    val purchaseUrl = testClient.addToCart(item.asin, qty)
    Redirect(purchaseUrl)
  }

  def signUp(email: String, password: String) = Action {
    val user = User(null, null, null, email, password, new java.util.ArrayList[ProductItem]())
    User.save(user)
    Redirect(routes.Application.items())
  }

  def start = Action {
    //Ok(views.html.login(null))
    Ok(views.html.storeFront(false))
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
    if (User.emailExisted(email)){
//      Redirect(routes.Application.signUp).flashing(Flash(signUpForm.data) +
//        ("error" -> Messages("contact.validation.errors")))
//      BadRequest(views.html.login(signUpForm))
      BadRequest(views.html.login(
        signUpForm.fill("firstName", "lastName", "email", "password")
          .withGlobalError("Email " + email + " already registered")))
//
//        InternalServerError(views.html.login())
    }
    else {
      val user = User("", firstName, lastName, email, password, new util.ArrayList[ProductItem]())
      User.save(user)
      Redirect(routes.Application.prodSearch())
        .withSession(session + (SessionNameUserId -> user.id))
    }
   }
  }

  def signIn() = Action {implicit request =>{
    signUpForm.discardingErrors
    val data = signUpForm.bindFromRequest.data
    val (email, password) = (
      data.get("email").get,
      data.get("password").get)
    if (!User.emailExisted(email)){
      //      Redirect(routes.Application.signUp).flashing(Flash(signUpForm.data) +
      //        ("error" -> Messages("contact.validation.errors")))
      //      BadRequest(views.html.login(signUpForm))
      BadRequest(views.html.login(
        signUpForm.fill("firstName", "lastName", "email", "password")
          .withGlobalError("Email " + email + " is not registered")))
    }
    else {
      val user = User.findByEmail(email)
      if (user.password.equalsIgnoreCase(password)){
        BUtil.sendEmail(user.email, user.firstName)
        Redirect(routes.Application.items())
          .withSession(session + (SessionNameUserId -> user.id))
      }
      else
        BadRequest(views.html.login(
          signUpForm.fill("firstName", "lastName", "email", "password")
            .withGlobalError("invalid password for email  " + email)))
    }
  }
  }

  def signOut = Action { implicit request => {
    Redirect(routes.Application.signUp()).withNewSession
  }
    //    Ok(views.html.index("Your new application is ready."))
  }

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
    val userId = session.get(SessionNameUserId)
    if (isAdminUser)
      Ok(views.html.admin())
    else
      Ok(views.html.adminUnauthorized())
  } }


  def adminUserList = Action {implicit request =>{
    val users = User.all()
    Ok(views.html.adminUserList(users))
  } }

  def adminItemList = Action {implicit request =>{
    val items = ProductItem.all()
    Ok(views.html.adminItemList(items))
  } }

}