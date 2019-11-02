package project

import project.model.{Author, Book}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

object Boot extends App with ElasticSerializer{

  BookRepository.CreateBook

  implicit val system: ActorSystem = ActorSystem("book-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val bookRepository = system.actorOf(BookRepository.props(), "book-manager")

  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
      pathPrefix("library") {
        path("book" / Segment) { id =>
          get {
            complete {
              (bookRepository ? BookRepository.ReadBook(id)).mapTo[Either[ErrorResponse, Book]]
            }
          }
        } ~
          path("book") {
            post {
              entity(as[Book]) { book =>
                complete {
                  (bookRepository ? BookRepository.CreateBook(book)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
                }
              }
            }
          } ~
          path("book" / Segment) { id =>
            delete {
              complete {
                (bookRepository ? BookRepository.DeleteBook(id)).mapTo[Either[ErrorResponse, Book]]
              }
            }
          } ~
          path("book") {
            put {
              entity(as[Book]) { book =>
                complete {
                  (bookRepository ? BookRepository.UpdateBook(book)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
                }
              }
            }
          }
      }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)


}