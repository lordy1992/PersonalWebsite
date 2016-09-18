package controllers

import javax.inject._

import dao.{ArticleDao, MessageDao, ResumeDao}
import objects._
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.db.Database
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.mvc._
import play.api.libs.functional.syntax._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(db: Database) extends Controller with Secured {

  implicit val title = "Jeremy Lord"

  val articleDao = new ArticleDao(db)
  val messageDao = new MessageDao(db)
  val resumeDao = new ResumeDao()

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

  def resume = Action { request =>
    val resumeObj = resumeDao.getResumeFromJson("data/resume.json")
    val isAdmin = request.session.get(Security.username).isDefined

    Ok(views.html.resume(resumeObj, isAdmin, false))
  }

  def resume_edit = withAuth { username => implicit  request =>
    val resumeObj = resumeDao.getResumeFromJson("data/resume.json")
    val isAdmin = request.session.get(Security.username).isDefined

    Logger.info("In the edit logger!")
    Ok(views.html.resume(resumeObj, true, true))
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

  def update_resume_heading = withAuth { username => implicit request =>
    // TODO: Add validation to the parameters
    request.body.asJson.map { json =>
      val resumeName = (json \ "resumeName").as[String]
      val resumeJobTitle = (json \ "resumeJobTitle").as[String]
      val resumeCurrentCompany = (json \ "resumeCurrentCompany").as[String]
      val resumePhoneNo = (json \ "resumePhoneNo").as[String]
      val resumeEmail = (json \ "resumeEmail").as[String]
      val resumeHeader = ResumeHeader(resumeName, resumeJobTitle, resumeCurrentCompany, resumePhoneNo, resumeEmail)
      resumeDao.updateResumeHeader("data/resume.json", resumeHeader)

      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_summary = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      val resumeSummary = (json \ "summaryContent").as[String]
      resumeDao.updateResumeSummary("data/resume.json", resumeSummary)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_research_interests = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      val researchInterests = (json \ "researchInterestContent").as[String]
      resumeDao.updateResearchInterests("data/resume.json", researchInterests)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_past_experience = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      implicit val pastExperienceReader: Reads[(String, String, String)] = (
        (JsPath \ "jobTitle").read[String] and
        (JsPath \ "company").read[String] and
        (JsPath \ "description").read[String]
      ).tupled

      val tupList = json.as[List[(String, String, String)]]
      val experiences = tupList.map { case (jobTitle, company, description) =>
        Experience(jobTitle, company, description, Nil)
      }

      resumeDao.updateExperience("data/resume.json", experiences)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }
}
