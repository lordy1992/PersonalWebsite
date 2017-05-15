package filters

import javax.inject.Inject

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

/**
  * A filter to redirect HTTP to HTTPS.
  */
class TLSFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  override def apply(nextFilter: RequestHeader => Future[Result])(header: RequestHeader): Future[Result] = {
    Logger.info("In the TLSFilter")
    if (!header.secure) {
      // This is not using HTTPS
      Future.successful(Results.MovedPermanently("https://" + header.host + header.uri))
    } else {
      nextFilter(header).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
    }
  }
}
