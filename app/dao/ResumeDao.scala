package dao

import java.io.{File, FileInputStream, PrintWriter}

import objects._
import play.api.Logger
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

class ResumeDao(path: String) {

  def getResumeFromJson() : Resume = {
    val resumeStream = new FileInputStream(path + "resume.json")
    val resumeJson = try {
      Json.parse(resumeStream)
    } finally {
      resumeStream.close()
    }

    implicit val techSkillSummaryReads: Reads[TechSkillSummary] = (
      (JsPath \ "computerSkills").read[Seq[String]] and
      (JsPath \ "toolSkills").read[Seq[String]] and
      (JsPath \ "conceptualSkills").read[Seq[String]]
    )(TechSkillSummary.apply _)

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
      (JsPath \ "skillSummary").read[TechSkillSummary] and
      (JsPath \ "researchInterests").read[String] and
      (JsPath \ "pastExperience").read[Seq[Experience]] and
      (JsPath \ "expertise").read[Seq[String]] and
      (JsPath \ "skillBars").read[Seq[SkillBar]] and
      (JsPath \ "education").read[Seq[Education]]
    )(Resume.apply _)

    resumeJson.as[Resume]
  }

  def writeResumeToJson(resume: Resume) : Unit = {
    implicit val techSkillSummaryWrites: Writes[TechSkillSummary] = (
      (JsPath \ "computerSkills").write[Seq[String]] and
      (JsPath \ "toolSkills").write[Seq[String]] and
      (JsPath \ "conceptualSkills").write[Seq[String]]
    )(unlift(TechSkillSummary.unapply))

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
      (JsPath \ "skillSummary").write[TechSkillSummary] and
      (JsPath \ "researchInterests").write[String] and
      (JsPath \ "pastExperience").write[Seq[Experience]] and
      (JsPath \ "expertise").write[Seq[String]] and
      (JsPath \ "skillBars").write[Seq[SkillBar]] and
      (JsPath \ "education").write[Seq[Education]]
    )(unlift(Resume.unapply))

    val resumeAsJson = Json.prettyPrint(Json.toJson(resume))
    val currentTimestamp = System.currentTimeMillis()
    val tmpFile = new File(path + s"tmp/$currentTimestamp.json")
    val fileWriter = new PrintWriter(tmpFile)

    try {
      fileWriter.write(resumeAsJson)
    } finally {
      fileWriter.close()
    }

    // Rename the original resume (keep backups)
    val originalFile = new File(path + "resume.json")
    val backupFile = new File(path + s"backups/resume-$currentTimestamp.json")
    if (originalFile.renameTo(backupFile)) {
      // Renaming succeeded
      if (!tmpFile.renameTo(originalFile)) {
        // We were not able to rename the temp file to the resume slot, log error
        Logger.error("Could not move the temporary file to " + path + "resume.json")
      }
    } else {
      // Could not rename to a backup file
      Logger.error("Could not move resume.json to a backup file!")
    }
  }

  def updateResumeHeader(resumeHeader: ResumeHeader) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, name = resumeHeader.name,
      jobTitle = resumeHeader.jobTitle, currentCompany = resumeHeader.currentCompany,
      phoneNo = resumeHeader.phoneNo, email = resumeHeader.email)

    writeResumeToJson(modifiedResume)
  }

  def updateResumeSummary(resumeSummary: TechSkillSummary) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, skillSummary = resumeSummary)

    writeResumeToJson(modifiedResume)
  }

  def updateResearchInterests(resumeResearchInterests: String) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, researchInterests = resumeResearchInterests)

    writeResumeToJson(modifiedResume)
  }

  def updateExperience(experiences: Seq[Experience]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, pastExperience = experiences)

    writeResumeToJson(modifiedResume)
  }

  def updateExpertise(expertise: Seq[String]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, expertise = expertise)

    writeResumeToJson(modifiedResume)
  }

  def updateSkillBars(skillBars: List[SkillBar]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, skillBars = skillBars)

    writeResumeToJson(modifiedResume)
  }

  def updateEducation(education: List[Education]) : Unit = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, education = education)

    writeResumeToJson(modifiedResume)
  }

  def updateProfileImage(fileRelativePath: String) = {
    val (resume, newTimeStamp) = fetchResumeForModification()
    val modifiedResume = resume.copy(lastUpdated = newTimeStamp, profileImageUrl = fileRelativePath)

    writeResumeToJson(modifiedResume)
  }

  private def fetchResumeForModification() : (Resume, Long) = {
    val resume = getResumeFromJson()
    val newTimeStamp = System.currentTimeMillis() / 1000

    (resume, newTimeStamp)
  }
}
