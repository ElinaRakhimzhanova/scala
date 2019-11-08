package project

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import libraryBot.Bot.log
import libraryBot.TelegramMessage
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

  val chat_id = -352088280
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

  def sending(replyTo: ActorRef, status: Int, message: String, isSuccessful: Boolean) = {
    if(isSuccessful)
      replyTo ! Right(SuccessfulResponse(status, message))
    replyTo ! Left(ErrorResponse(status, message))

  }

  def receive: Receive = {

    case CreateBook(book) => {
      val send = sender()
      //val book = Book("id-1", "Harry Potter", Author("dir-1", "Joan", "Rowling"), 2019, "fantasy")
      val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

      cmd.onComplete {
        case Success(value) =>
          log.info(s"New book with ID: ${book.id} created.")
          sending(send, 201, s"Book with ID: ${book.id} is created.", true)
          //sender() ! SuccessfulResponse(201, s"Book with ID: ${book.id} already exists.")
          var message = TelegramMessage(chat_id, s"Book with ID: ${book.id} is created.")
          TelegramManager(message)

        case Failure(fail) =>
          log.warning(s"Could not create a book with ID: ${book.id} because it already exists.")
          sending(send, 409, s"Book with ID: ${book.id} already exists.", false)
          //sender() ! ErrorResponse(409, s"Book with ID: ${book.id} already exists.")
      }
    }

    // pattern match either => case Right, case Left
    // Right  e.result.found maybe false => no data found in Elastic
    case ReadBook(id) => {
      val send = sender()
      client.execute {
        get(id).from("books" / "_doc")
      }.onComplete {
        case Success(either) =>
          either match {
            case Right(e) =>
              if(e.result.found) {
                e.result.to[Book]
                log.info(s"Book with id ${id}.")
                sending(send, 200, s"Book with ID: ${id}.", true)
                var message = TelegramMessage(chat_id, s"Book with ID: ${id}.")
                TelegramManager(message)
              }
              else {
                log.info(s"Book with id ${id} is found but deleted.")
                sending(send, 404, s"Book with ID: ${id} does not exists actually.", false)
              }
            case Left(error) =>
              log.info(s"Book with id ${id} is deleted.")
              sending(send, 404, s"Book with ID: ${id} does not exists.", false)
          }
        case Failure(fail) =>
          log.warning(s"Could not create a book with ID: ${id} because it already exists.")
          sending(send, 409, s"Book with ID: ${id} already exists.", false)
          //sender() ! ErrorResponse(409, s"Book with ID: already exists.")
        }
    }

    case UpdateBook(book) => {
      val send = sender()
      val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

      cmd.onComplete {
        case Success(either) =>
          either match {
            case Right(e) =>
//              if(e.result.found) {
//                e.result.to[Book]
//                log.info(s"Book with id ${book.id}.")
//                sending(send, 200, s"Book with ID: ${book.id} is updated.", true)
//              }
//              else {
//                log.info(s"Book with id ${book.id} is found but deleted.")
//                sending(send, 404, s"Book with ID: ${book.id} does not exists actually.", false)
//              }
              log.warning(s"Book with ID: ${book.id} updated.")
              sending(send, 200, s"Book with ID: ${book.id} updated.", true)
            case Left(error) =>
              log.info(s"Book with id ${book.id} is deleted.")
              sending(send, 404, s"Book with ID: ${book.id} does not exists.", false)
          }
//          log.warning(s"Book with ID: ${book.id} updated.")
//          sending(send, 200, s"Book with ID: ${book.id} updated.", true)
          //sender() ! SuccessfulResponse(200, s"Book with ID: ${book.id} updated.")
        case Failure(fail) =>
          log.warning(s"Could not update a book with ID: ${book.id} because it already does not exist.")
          sending(send, 404, s"Book with ID: ${book.id} does not exist.", false)
          //sender() ! ErrorResponse(404, s"Book with ID: ${book.id} does not exist.")
      }
    }

    case DeleteBook(id) => {
      val send = sender()
      client.execute {
        delete(id).from("books" / "_doc")
      }.onComplete {
        case Success(either) =>
//          either match {
//            case Right(e) =>
//              if(e.result.found) {
//                e.result.to[Book]
//                log.info(s"Book with id ${id} is deleted.")
//                sending(send, 200, s"Book with ID: ${id}.", true)
//              }
//              else {
//                log.info(s"Book with id ${id} is already deleted.")
//                sending(send, 404, s"Book with ID: ${id} already deleted.", false)
//              }
              log.warning(s"Book with ID: ${id} successfully deleted.")
              sending(send, 200, s"Book with ID: ${id} successfully deleted.", true)
              var message = TelegramMessage(chat_id, s"Book with ID: ${id} successfully deleted.")
              TelegramManager(message)

//            case Left(error) =>
//              log.info(s"Book with id ${id} is deleted.")
//              sending(send, 404, s"Book with ID: ${id} does not exists.", false)
          //log.warning(s"Book with ID: ${id} successfully deleted.")
          //sending(send, 200, s"Book with ID: ${id} successfully deleted.", true)
          //sender() ! SuccessfulResponse(200, s"Book with ID: ${id} successfully deleted.")
        case Failure(fail) =>
          log.warning(s"Could not delete a book with ID: because it does not exists.")
          sending(send, 404, s"Book with ID: ${id} does not exists.", false)
          //sender() ! ErrorResponse(404, s"Book with ID: ${id} does not exists.")
      }
    }
  }
}
