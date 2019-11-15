package week11.s3

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._

import scala.concurrent.ExecutionContextExecutor

object Aws extends App with Serializer {

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
        path("file") {
          get {
            parameters('fileName.as[String]) { path =>
              complete {
                (amazonManager ? AmazonManager.GetFile(path)).mapTo[Response]
              }
            }
          }
        } ~
          path("file") {
            post {
              entity(as[Body]) { body =>
                complete {
                  (amazonManager ? AmazonManager.PostFile(body.path)).mapTo[Response]
                }
              }
            }
          }


        val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
      }
}