package project

import akka.actor.{Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import project.model.{Author, Book}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object BookRepository {

  def props(): Props = Props(new BookRepository)

  case class CreateBook(book: Book)

  case class ReadBook(id : String)

  case class UpdateBook(book: Book)

  case class DeleteBook(id: String)

}

class BookRepository extends Actor with ActorLogging with ElasticSerializer {

  import BookRepository._

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  def createElasticIndex(): Unit = {
    val cmd: Future[Either[RequestFailure, RequestSuccess[CreateIndexResponse]]] =
      client.execute {
        createIndex("books")
      }
    cmd.onComplete {
      case Success(value) =>
        value.foreach {requestSuccess =>
          println(requestSuccess)}

      case Failure(exception) =>
        println(exception.getMessage)
    }
  }

  def receive: Receive = {

      case CreateBook => {
        val book = Book("id-1", "Harry Potter", Author("dir-1", "Joan", "Rowling"), 2019, "fantasy")
        val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

        cmd.onComplete {
          case Success(value) =>
            log.warning(s"Could not create a book with ID: because it already exists.")
            sender() ! SuccessfulResponse(201, s"Book with ID: already exists.")
            println(value)

          case Failure(fail) =>
            log.warning(s"Could not create a book with ID: because it already exists.")
            sender() ! ErrorResponse(409, s"Book with ID: already exists.")
            println(fail.getMessage)
          }
      }

      case ReadBook(id) => {
        client.execute {
          get(id).from("books" / "_doc")
        }.onComplete {
          case Success(either) =>
            either.map(e => e.result.to[Book]).foreach { book =>
              log.warning(s"Could not create a book with ID: because it already exists.")
              sender() ! SuccessfulResponse(200, s"Book with ID: already exists.")
              println(id)
            }
          case Failure(fail) =>
            log.warning(s"Could not create a book with ID: because it already exists.")
            sender() ! ErrorResponse(409, s"Book with ID: already exists.")
            println(fail.getMessage)
        }
      }

      case UpdateBook(book) => {
        val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

        cmd.onComplete {
          case Success(value) =>
            log.warning(s"Could not create a book with ID: because it already exists.")
            sender() ! SuccessfulResponse(200, s"Book with ID: already exists.")
            println(value)
          case Failure(fail) =>
            log.warning(s"Could not create a book with ID: because it already exists.")
            sender() ! ErrorResponse(409, s"Book with ID: already exists.")
            println(fail.getMessage)
        }
      }

      case DeleteBook(id) => {
        client.execute {
          delete(id).from("books" / "_doc")
        }.onComplete {
          case Success(either) =>
            log.warning(s"Could not create a book with ID:  because it already exists.")
            sender() ! SuccessfulResponse(200, s"Book with ID:  already exists.")
          case Failure(fail) =>
            log.warning(s"Could not delete a book with ID: because it does not exists.")
            sender() ! ErrorResponse(409, s"Book with ID: ${id} does not exists.")
            println(fail.getMessage)
        }
      }

    }
}
