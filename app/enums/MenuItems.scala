package enums

/**
  * Created by jlord on 5/8/2016.
  */
object MenuItems {

  sealed abstract class MenuItem(
    name : String,
    path : String) {

    def getName = name
    def getPath = path
    override def toString = name
  }

  case object HOME extends MenuItem("Home", "/")
  case object ABOUT extends MenuItem("About", "/about")
  case object CONTACT extends MenuItem("Contact", "/contact")
  case object PROGRAMMING extends MenuItem("Programming", "/programming")
  case object WRITING extends MenuItem("Writing", "/writing")
  case object RESUME extends MenuItem("Resume", "/resume")
  case object NONE extends MenuItem("None", "/")
}
