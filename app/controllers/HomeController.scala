package controllers

import javax.inject._

import dao.{ArticleDao, MessageDao}
import objects._
import play.api.data.Forms._
import play.api.data._
import play.api.db.Database
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(db: Database) extends Controller with Secured {

  implicit val title = "Jeremy Lord"

  val articleDao = new ArticleDao(db)
  val messageDao = new MessageDao(db)

  val articleForm = Form(
    mapping(
      "article-name" -> text,
      "article" -> text
    )(Article.apply)(Article.unapply)
  )

  val messageForm = Form(
    mapping(
      "message-subject" -> text,
      "message-author" -> text,
      "message" -> text
    )(Message.apply)(Message.unapply)
  )

  val deleteForm = Form(
    "messageid" -> number
  )

  def index = Action {
    Ok(views.html.main())
  }

  def about = Action {
    Ok(views.html.about())
  }

  def contact = Action {
    Ok(views.html.contact())
  }

  def technical = Action {
    Ok(views.html.technical())
  }

  def writing = Action {
    Ok(views.html.writing(articleDao.listAllArticles()))
  }

  def post_message = Action { implicit request =>
    val messageData = messageForm.bindFromRequest.get
    messageDao.addMessage(messageData)
    Ok(views.html.contact())
  }

  def admin = withAuth { username => implicit request =>
    Ok(views.html.admin(messageDao.listAllMessages()))
  }

  def post_article = withAuth { username => implicit request =>
    val articleData = articleForm.bindFromRequest.get
    articleDao.addArticle(articleData)
    Redirect(controllers.routes.HomeController.writing())
  }

  def delete_message = withAuth { username => implicit request =>
    val messageId = deleteForm.bindFromRequest.get
    messageDao.removeMessage(messageId)
    Redirect(controllers.routes.HomeController.admin())
  }
}
