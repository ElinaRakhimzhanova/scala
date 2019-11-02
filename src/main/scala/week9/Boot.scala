package week9

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import week6.model.{Director, Movie}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Boot extends App with ElasticSerializer {

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  def createEsIndex() = {
    val cmd: Future[Either[RequestFailure, RequestSuccess[CreateIndexResponse]]] =
      client.execute { createIndex("movies") }

    cmd.onComplete {
      case Success(value) =>
        value.foreach {requestSuccess =>
          println(requestSuccess)}

      case Failure(exception) =>
        println(exception.getMessage)
    }
  }

  def createMovie: Unit = {

    val movie = Movie("id-1", "Joker", Director("dir-1", "Todd", None, "Philips"), 2019)  // object

    // object => JSON (???)


    // 1) object -> (json serializer) -> string.   String -> ES


    // 2) object (auto (de)serializer) -> Json String

    val cmd = client.execute(indexInto("movies" / "_doc").id("id-1").doc(movie))

    cmd.onComplete {
      case Success(value) =>
        println(value)

      case Failure(fail) =>
        println(fail.getMessage)
    }
  }

  def readMovie(id: String): Unit = {
    client.execute {
      get(id).from("movies" / "_doc")
    }.onComplete {
      case Success(either) =>
        either.map ( e => e.result.to[Movie] ).foreach { movie =>
          println(movie)
        }
      case Failure(fail) =>
    }
  }



  //createMovie
  //readMovie("id-1")




}
