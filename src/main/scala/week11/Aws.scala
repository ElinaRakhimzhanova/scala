package week11

import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.model.Uri.Path.Segment
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import scala.concurrent.ExecutionContextExecutor

object Aws extends App {

  implicit val system: ActorSystem = ActorSystem("book-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val amazonManager = system.actorOf(AmazonManager.props(), "amazon-manager")

  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
      pathPrefix("library") {
        path("file" / Segment) { file =>
          get {
            complete {
              (amazonManager ? AmazonManager.GetFile(file)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        } ~
          path("file") {
            post { file =>
                complete {
                  (amazonManager ? AmazonManager.PostFile(file)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
                }
              }
            }
          }

        val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

      }
}

