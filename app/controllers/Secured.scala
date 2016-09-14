package controllers

import play.api.mvc.{Result, _}

/**
  * Created by jlord on 5/8/2016.
  */
trait Secured {
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.AuthController.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}
