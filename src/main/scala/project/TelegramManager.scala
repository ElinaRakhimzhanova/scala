package project

import scala.util.{Failure, Success}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import libraryBot.TelegramMessage
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

case class TelegramManager(message: TelegramMessage) extends Serializer {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config: Config = ConfigFactory.load()
  val log = LoggerFactory.getLogger("TelegramManager")

  val httpReq = Marshal(message).to[RequestEntity].flatMap { entity =>
    val request = HttpRequest(HttpMethods.POST, s"https://api.telegram.org//sendMessage", Nil, entity)
    log.debug("Request: {}", request)
    Http().singleRequest(request)
  }

  httpReq.onComplete {
    case Success(value) =>
      log.info(s"Response: $value")
      value.discardEntityBytes()

    case Failure(exception) =>
      log.error("error")
  }
}
