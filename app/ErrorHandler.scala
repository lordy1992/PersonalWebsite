import javax.inject._

import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._


/**
  * An error handler that extends the default error handler so that the Devo error page continues to show the useful
  * information that Play provides for debugging, and so that in production this is not shown.
  */
@Singleton
class ErrorHandler @Inject() (
     env: Environment,
     config: Configuration,
     sourceMapper: OptionalSourceMapper,
     router: Provider[Router]) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      statusCode match {
        case 404 => Results.NotFound(views.html.notFound("Jeremy Lord | Page Not Found"))
        case _ => Results.Status(statusCode)("Client error " + statusCode + ". Message: " + message)
      }
    )
  }

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Future.successful(
      Results.InternalServerError("An unexpected error has occurred.")
    )
  }
}
