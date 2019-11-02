package project

import akka.actor.{Actor, ActorLogging}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import project.model.Book

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class BookRepository extends Actor with ActorLogging with ElasticSerializer {

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

  def createBook(book: Book): Unit = {

    //val book = Book("id-1", "Harry Potter", Author("dir-1", "Joan", "Rowling"), 2019, "fantasy")

    val cmd = client.execute(indexInto("books" / "_doc").id("id-1").doc(book))

    cmd.onComplete {
      case Success(value) =>
        log.warning(s"Could not create a movie with ID: because it already exists.")
        sender() ! SuccessfulResponse(201, s"Movie with ID: already exists.")
        println(value)

      case Failure(fail) =>
        log.warning(s"Could not create a movie with ID: because it already exists.")
        sender() ! ErrorResponse(409, s"Movie with ID: already exists.")
        println(fail.getMessage)
    }
  }

  def readBook(id : String): Unit = {
    client.execute {
      get(id).from("books" / "_doc")
    }.onComplete {
      case Success(either) =>
        either.map ( e => e.result.to[Book] ).foreach { movie =>
          log.warning(s"Could not create a movie with ID: because it already exists.")
          sender() ! SuccessfulResponse(200, s"Movie with ID: already exists.")
          println(movie)
        }
      case Failure(fail) =>
        log.warning(s"Could not create a movie with ID: because it already exists.")
        sender() ! ErrorResponse(409, s"Movie with ID: already exists.")
        println(fail.getMessage)
    }
  }

  def updateBook(book : Book): Unit = {
    val cmd = client.execute(indexInto("books" / "_doc").id(book.id).doc(book))

    cmd.onComplete {
      case Success(value) =>
        log.warning(s"Could not create a movie with ID: because it already exists.")
        sender() ! SuccessfulResponse(200, s"Movie with ID: already exists.")
        println(value)
      case Failure(fail) =>
        log.warning(s"Could not create a movie with ID: because it already exists.")
        sender() ! ErrorResponse(409, s"Movie with ID: already exists.")
        println(fail.getMessage)
    }
  }

  def deleteBook(id : String): Unit = {
    client.execute {
      get(id).from("books" / "_doc")
    }.onComplete {
      case Success(either) =>
        log.warning(s"Could not create a movie with ID:  because it already exists.")
        sender() ! SuccessfulResponse(200, s"Movie with ID:  already exists.")
        either == null
      case Failure(fail) =>
        log.warning(s"Could not create a movie with ID: because it already exists.")
        sender() ! ErrorResponse(409, s"Movie with ID: already exists.")
        println(fail.getMessage)
    }
  }

  override def receive: Receive = ???
}
