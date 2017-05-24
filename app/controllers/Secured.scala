package controllers

import java.util.Calendar

import play.api.Logger
import play.api.mvc.{Result, _}

trait Secured {
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.AuthController.login)

  def withAuth(f: => String => Request[AnyContent] => Result) : EssentialAction = {
    withAuth(BodyParsers.parse.anyContent)(f)
  }

  /*
   * This is wrapped around actions that should only be allowed for authenticated users. In addition, it handles
   * a server-side session timeout. Whenever this is called (when updates are made), the timeout is reset. If it
   * times out, the user is redirected to the login page and the session is cleared.
   */
  def withAuth[A](p: BodyParser[A])(f: => String => Request[A] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(p)(request => {
        val previousTime = request.session.get("userTime") getOrElse ""
        val currentTime = Calendar.getInstance.getTimeInMillis
        if (!previousTime.isEmpty) {
          val previousTimeLong = previousTime.toLong
          val timeoutInMilliseconds = 900000

          Logger.info("Current time: " + currentTime + ", and prev time: " +
            previousTimeLong + ", Difference: " + (currentTime - previousTimeLong))
          if ((currentTime - previousTimeLong) > timeoutInMilliseconds) {
            // The session has expired
            onUnauthorized(request).withNewSession.flashing("success" -> "You have been logged out")
          } else {
            f(user)(request).withSession(request.session + ("userTime" -> currentTime.toString))
          }
        } else {
          f(user)(request).withSession(request.session + ("userTime" -> currentTime.toString))
        }
      })
    }
  }
}
