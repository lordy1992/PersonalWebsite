package enums

/**
  * Created by jlord on 10/18/2016.
  */
object PostStatus {
  sealed abstract class PostStatus(status: String) {
    def getName = status
    override def toString = status
  }

  case object EDIT extends PostStatus("EDIT")
  case object PUBLISHED extends PostStatus("PUBLISHED")
  case object DELETED extends PostStatus("DELETED")
  val validStatuses = List(EDIT, PUBLISHED, DELETED)

  def getStatusFromName(name: String): Option[PostStatus] = {
    validStatuses.filter(p => p.getName == name).headOption
  }
}
