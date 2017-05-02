package controllers

import java.io.{BufferedReader, FileReader}
import javax.inject.{Inject, Singleton}

import org.mindrot.jbcrypt.BCrypt
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

/**
  * Created by jlord on 5/8/2016.
  */
@Singleton
class AuthController @Inject()(val messagesApi: MessagesApi, configuration: play.api.Configuration)
    extends Controller with I18nSupport {

  implicit val title = "Jeremy Lord"

  val loginForm = Form(
    tuple(
      "Username" -> text,
      "Password" -> text
    ) verifying ("Invalid email or password", result => result match  {
      case (username, password) => check(username, password)
    })
  )

  def check(username: String, password: String) = {

    val adminHashBr = new BufferedReader(new FileReader(configuration.underlying.getString("admin.path")))
    val hashInput = adminHashBr.readLine()
    adminHashBr.close()

    val usernameHashCombo = hashInput.split(":", 2)

    (username == usernameHashCombo(0) && BCrypt.checkpw(password, usernameHashCombo(1)))
  }

  def login = Action {
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.HomeController.admin).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}
