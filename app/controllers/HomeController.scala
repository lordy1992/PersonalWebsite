package controllers

import java.io.FileInputStream
import javax.inject._

import dao.{ArticleDao, MessageDao}
import objects._
import play.api.Logger
import play.api.data._
import play.api.data.Forms._
import play.api.db.Database
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._
import play.api.mvc._
import java.util.Date

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

  def programming = Action {
    Ok(views.html.programming())
  }

  def writing = Action {
    Ok(views.html.writing(articleDao.listAllArticles()))
  }

  def resume = Action {
    val resumeStream = new FileInputStream("data/resume.json")
    val resumeJson = try {
      Json.parse(resumeStream)
    } finally {
      resumeStream.close()
    }

    implicit val educationReads: Reads[Education] = (
      (JsPath \ "degree").read[String] and
      (JsPath \ "year").read[Int] and
      (JsPath \ "gpa").read[String]
    )(Education.apply _)

    implicit val skillBarReads: Reads[SkillBar] = (
      (JsPath \ "skill").read[String] and
      (JsPath \ "progress").read[Int] and
      (JsPath \ "style").read[String]
    )(SkillBar.apply _)

    implicit val pastExperienceReads: Reads[Experience] = (
      (JsPath \ "jobTitle").read[String] and
      (JsPath \ "company").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "additionalPoints").read[Seq[String]]
    )(Experience.apply _)

    implicit val resumeReads: Reads[Resume] = (
      (JsPath \ "lastUpdated").read[Long] and
      (JsPath \ "profileImageUrl").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "jobTitle").read[String] and
      (JsPath \ "currentCompany").read[String] and
      (JsPath \ "phoneNo").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "summary").read[String] and
      (JsPath \ "researchInterests").read[String] and
      (JsPath \ "pastExperience").read[Seq[Experience]] and
      (JsPath \ "expertise").read[Seq[String]] and
      (JsPath \ "skillBars").read[Seq[SkillBar]] and
      (JsPath \ "education").read[Seq[Education]]
    )(Resume.apply _)

    val resumeObj = resumeJson.as[Resume]

    Ok(views.html.resume(resumeObj))
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
