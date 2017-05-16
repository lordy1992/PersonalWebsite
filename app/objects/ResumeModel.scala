package objects

abstract class ResumeModel

case class Resume(val lastUpdated: Long,
                  val profileImageUrl: String,
                  val name: String,
                  val jobTitle: String,
                  val currentCompany: String,
                  val phoneNo: String,
                  val email: String,
                  val skillSummary: TechSkillSummary,
                  val researchInterests: String,
                  val pastExperience: Seq[Experience],
                  val expertise: Seq[String],
                  val skillBars: Seq[SkillBar],
                  val education: Seq[Education]) extends ResumeModel

case class Experience(val jobTitle: String,
                      val company: String,
                      val description: String,
                      val additionalPoints: Seq[String]) extends ResumeModel

case class SkillBar(val skill: String,
                    val progress: Int) extends ResumeModel

case class Education(val degree: String,
                     val year: Int,
                     val gpa: String) extends ResumeModel

case class TechSkillSummary(val computerSkills: Seq[String],
                            val toolSkills: Seq[String],
                            val conceptualSkills: Seq[String]) extends ResumeModel