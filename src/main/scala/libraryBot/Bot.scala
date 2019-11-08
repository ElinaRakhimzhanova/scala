package libraryBot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Bot extends App with Serializer {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config: Config = ConfigFactory.load() // config
  val log = LoggerFactory.getLogger("Bot")

  //val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  val token = config.getString("telegram.token") // token
  log.info(s"Token: $token")

  val message: TelegramMessage = TelegramMessage(276182704, "Hello library")

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

  Thread.sleep(10000)

}

//chat_id = 276182704

