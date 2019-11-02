package project

import akka.actor.{Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Boot extends App {

  //createBook
  //readBook("id-1")
  //updateBook(Book("id-1", "Harry Potter", Author("dir-1", "Joan", "Rouling"), 2010, "fantasy"))
  //deleteBook("id-1")

}