package project

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
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
        myfun(sender(), id)
        val book = Book("id-1", "Harry Potter", Author("dir-1", "Joan", "Rowling"), 2019, "fantasy")
        val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

        cmd.onComplete {
          case Success(value) =>
            log.warning(s"New book with ID: ${book.id} created.")
            //sender() ! SuccessfulResponse(201, s"Book with ID: ${book.id} already exists.")
            println(value)

          case Failure(fail) =>
            log.warning(s"Could not create a book with ID: ${book.id} because it already exists.")
            //sender() ! ErrorResponse(409, s"Book with ID: ${book.id} already exists.")
            println(fail.getMessage)
          }
      }

      case ReadBook(id) => {
        myfun(sender(), id)

        client.execute {
          get(id).from("books" / "_doc")
        }.onComplete {
          case Success(either) =>
            // pattern match either => case Right, case Left
            // Right  e.result.found maybe false => no data found in Elastic
            either.map(e => {
              case Right(e.result.found) =>
                if(true) {
                  e.result.to[Book]
                  log.info(s"Book with id ${id}.")
                }
                else {
                  sender() ! ErrorResponse(404, s"Book with ID: ${id}.")
                }
              case Left(error) =>
                println(error)
            })
              //either.map(e => e.result.to[Book]).foreach { book =>
              log.warning(s"Book with id ${id}.")
              sender() ! SuccessfulResponse(200, s"Book with ID: ${id}.")
              //println(id)

          case Failure(fail) =>
            log.warning(s"Could not create a book with ID: ${id} because it already exists.")
            //sender() ! ErrorResponse(409, s"Book with ID: already exists.")
            println(fail.getMessage)
        }
      }

      case UpdateBook(book) => {
        myfun(sender(), book)
        val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

        cmd.onComplete {
          case Success(value) =>
            log.warning(s"Book with ID: ${book.id} updated.")
            //sender() ! SuccessfulResponse(200, s"Book with ID: ${book.id} updated.")
            println(value)
          case Failure(fail) =>
            log.warning(s"Could not update a book with ID: ${book.id} because it already does not exist.")
            //sender() ! ErrorResponse(404, s"Book with ID: ${book.id} does not exist.")
            println(fail.getMessage)
        }
      }

      case DeleteBook(id) => {
        myfun(sender(), id)
        client.execute {
          delete(id).from("books" / "_doc")
        }.onComplete {
          case Success(either) =>
            log.warning(s"Book with ID: ${id} successfully deleted.")
            //sender() ! SuccessfulResponse(200, s"Book with ID: ${id} successfully deleted.")
          case Failure(fail) =>
            log.warning(s"Could not delete a book with ID: because it does not exists.")
            //sender() ! ErrorResponse(404, s"Book with ID: ${id} does not exists.")
            println(fail.getMessage)
        }
      }

    }

  def myfun(replyTo: ActorRef, id: String, book: Book) = {

    replyTo ! Right(SuccessfulResponse(200, s"Book with ID: ${id} successfully deleted."))
    replyTo ! Left(ErrorResponse(404, s"Book with ID: ${id} does not exists."))


  }
}
