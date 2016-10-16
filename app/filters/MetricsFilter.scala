package filters

import javax.inject._

import akka.stream.Materializer
import play.Logger
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by jlord on 10/15/2016.
  */
class MetricsFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  override def apply(filter: (RequestHeader) => Future[Result])(header: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis

    filter(header).map { result =>
      val endTime = System.currentTimeMillis
      val timeDiff = endTime - startTime

      Logger.info(s"${header.method} ${header.uri} took ${timeDiff} ms and returned a status of ${result.header.status}")

      result.withHeaders("Request-Time" -> timeDiff.toString)
    }
  }
}
