package controllers

import javax.inject.{Inject, Singleton}

import dao.ResumeDao
import objects.{Education, Experience, ResumeHeader, SkillBar}
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.mvc.{Action, Controller, Security}
import play.api.libs.functional.syntax._

/**
  * Created by jlord on 10/16/2016.
  */
@Singleton
class ResumeController @Inject() (configuration: play.api.Configuration) extends Controller with Secured {

  implicit val title = "Jeremy Lord"
  val resumeDao = new ResumeDao(configuration.underlying.getString("resume.path"))

  def resume = Action { request =>
    val resumeObj = resumeDao.getResumeFromJson()
    val isAdmin = request.session.get(Security.username).isDefined

    Ok(views.html.resume_view.resume(resumeObj, isAdmin, false))
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
      resumeDao.updateResumeHeader(resumeHeader)

      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_summary = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      val resumeSummary = (json \ "summaryContent").as[String]
      resumeDao.updateResumeSummary(resumeSummary)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_research_interests = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      val researchInterests = (json \ "researchInterestContent").as[String]
      resumeDao.updateResearchInterests(researchInterests)
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

      resumeDao.updateExperience(experiences)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_expertise_info = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      val expertiseList = json.as[List[String]]

      resumeDao.updateExpertise(expertiseList)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_language_tech_info = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      implicit val techSkillReader: Reads[(String, Int)] = (
        (JsPath \ "skill").read[String] and
          (JsPath \ "progress").read[Int]
        ).tupled

      val languageTechList = json.as[List[(String, Int)]]
      val skillBars =  languageTechList.sortBy(- _._2).map { case (skill, progress) =>
        SkillBar(skill, progress)
      }

      resumeDao.updateSkillBars(skillBars)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def update_education = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      implicit val educationReader: Reads[(String, Int, String)] = (
        (JsPath \ "degree").read[String] and
          (JsPath \ "year").read[Int] and
          (JsPath \ "gpa").read[String]
        ).tupled

      val educationList = json.as[List[(String, Int, String)]]
      val education = educationList.map { case (degree, year, gpa) =>
        Education(degree, year, gpa)
      }

      resumeDao.updateEducation(education)
      Ok(Json.obj("status" -> "Success"))
    }.getOrElse {
      BadRequest(Json.obj("status" -> "Failure"))
    }
  }

  def upload_resume_image = withAuth(parse.multipartFormData) { username => implicit request =>
    request.body.file("profileImage").map { profileImage =>
      import java.io.File
      if (profileImage.contentType.isDefined && extensionFromContentType.isDefinedAt(profileImage.contentType.get)) {
        val extension = extensionFromContentType(profileImage.contentType.get)
        val fileRelativePath = "images/profile-image" + extension
        val dataPath = new File(play.Environment.simple().rootPath(), "/public/" + fileRelativePath)
        profileImage.ref.moveTo(dataPath)
        resumeDao.updateProfileImage(fileRelativePath)
        Redirect(controllers.routes.ResumeController.resume())
      } else {
        UnsupportedMediaType("File type not accepted")
      }
    }.getOrElse {
      BadRequest("File not uploaded")
    }
  }

  private val extensionFromContentType: PartialFunction[String, String] = {
    case "image/bmp" => ".bmp"
    case "image/jpeg" => ".jpg"
    case "image/png" => ".png"
  }
}
