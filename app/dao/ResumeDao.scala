package dao

import java.io.{File, FileInputStream, PrintWriter}

import objects._
import play.api.Logger
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

/**
  * Created by jlord on 9/16/2016.
  */
class ResumeDao {

  def getResumeFromJson(path: String) : Resume = {
    val resumeStream = new FileInputStream(path)
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
      (JsPath \ "progress").read[Int]
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

    resumeJson.as[Resume]
  }

  def writeResumeToJson(path: String, resume: Resume) : Unit = {
    implicit val educationWrites: Writes[Education] = (
      (JsPath \ "degree").write[String] and
      (JsPath \ "year").write[Int] and
      (JsPath \ "gpa").write[String]
    )(unlift(Education.unapply))

    implicit val skillBarWrites: Writes[SkillBar] = (
      (JsPath \ "skill").write[String] and
      (JsPath \ "progress").write[Int]
    )(unlift(SkillBar.unapply))

    implicit val pastExperienceReads: Writes[Experience] = (
      (JsPath \ "jobTitle").write[String] and
      (JsPath \ "company").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "additionalPoints").write[Seq[String]]
    )(unlift(Experience.unapply))

    implicit val resumeReads: Writes[Resume] = (
      (JsPath \ "lastUpdated").write[Long] and
      (JsPath \ "profileImageUrl").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "jobTitle").write[String] and
      (JsPath \ "currentCompany").write[String] and
      (JsPath \ "phoneNo").write[String] and
      (JsPath \ "email").write[String] and
      (JsPath \ "summary").write[String] and
      (JsPath \ "researchInterests").write[String] and
      (JsPath \ "pastExperience").write[Seq[Experience]] and
      (JsPath \ "expertise").write[Seq[String]] and
      (JsPath \ "skillBars").write[Seq[SkillBar]] and
      (JsPath \ "education").write[Seq[Education]]
    )(unlift(Resume.unapply))

    val resumeAsJson = Json.prettyPrint(Json.toJson(resume))
    val currentTimestamp = System.currentTimeMillis()
    val tmpFile = new File(s"tmp/$currentTimestamp.json")
    val fileWriter = new PrintWriter(tmpFile)

    try {
      fileWriter.write(resumeAsJson)
    } finally {
      fileWriter.close()
    }

    // Rename the original resume (keep backups)
    val originalFile = new File(path)
    val backupFile = new File(s"data/backups/resume-$currentTimestamp.json")
    if (originalFile.renameTo(backupFile)) {
      // Renaming succeeded
      if (!tmpFile.renameTo(originalFile)) {
        // We were not able to rename the temp file to the resume slot, log error
        Logger.error("Could not move the temporary file to /data/resume.json")
      }
    } else {
      // Could not rename to a backup file
      Logger.error("Could not move resume.json to a backup file!")
    }
  }

  def updateResumeHeader(path: String, resumeHeader: ResumeHeader) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, name = resumeHeader.name,
      jobTitle = resumeHeader.jobTitle, currentCompany = resumeHeader.currentCompany,
      phoneNo = resumeHeader.phoneNo, email = resumeHeader.email)

    writeResumeToJson(path, modifiedResume)
  }

  def updateResumeSummary(path: String, resumeSummary: String) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, summary = resumeSummary)

    writeResumeToJson(path, modifiedResume)
  }

  def updateResearchInterests(path: String, resumeResearchInterests: String) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, researchInterests = resumeResearchInterests)

    writeResumeToJson(path, modifiedResume)
  }

  def updateExperience(path: String, experiences: Seq[Experience]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, pastExperience = experiences)

    writeResumeToJson(path, modifiedResume)
  }

  def updateExpertise(path: String, expertise: Seq[String]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, expertise = expertise)

    writeResumeToJson(path, modifiedResume)
  }

  def updateSkillBars(path: String, skillBars: List[SkillBar]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, skillBars = skillBars)

    writeResumeToJson(path, modifiedResume)
  }

  def updateEducation(path: String, education: List[Education]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification(path)
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, education = education)

    writeResumeToJson(path, modifiedResume)
  }

  private def fetchResumeForModification(path: String) : (Resume, Long) = {
    val resume = getResumeFromJson(path)
    val newTimeStamp = System.currentTimeMillis() / 1000

    (resume, newTimeStamp)
  }
}
