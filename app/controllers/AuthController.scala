package controllers

import javax.inject.{Inject, Singleton}

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.i18n.{I18nSupport, MessagesApi}

/**
  * Created by jlord on 5/8/2016.
  */
@Singleton
class AuthController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  implicit val title = "Jeremy Lord"

  val loginForm = Form(
    tuple(
      "Username" -> text,
      "Password" -> text
    ) verifying ("Invalid email or password", result => result match  {
      case (username, password) => check(username, password)
    })
  )

  //TODO: Actually implement authorization.
  def check(username: String, password: String) = {
    (username == "admin" && password == "1234")
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
