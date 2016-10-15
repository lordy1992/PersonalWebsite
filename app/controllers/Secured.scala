package controllers

import play.api.mvc.{Result, _}

/**
  * Created by jlord on 5/8/2016.
  */
trait Secured {
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.AuthController.login)

  def withAuth(f: => String => Request[AnyContent] => Result) : EssentialAction = {
    withAuth(BodyParsers.parse.anyContent)(f)
  }

  def withAuth[A](p: BodyParser[A])(f: => String => Request[A] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(p)(request => f(user)(request))
    }
  }
}
