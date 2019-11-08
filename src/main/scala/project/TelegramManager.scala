package project

import scala.util.{Failure, Success}
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity}
import libraryBot.TelegramMessage

object TelegramManager {

    val logging = logging
    val message: TelegramMessage = TelegramMessage(276182704, message)

    val httpReq = Marshal(message).to[RequestEntity].flatMap { entity =>
      val request = HttpRequest(HttpMethods.POST, s"https://api.telegram.org/bot929725600:AAEPwGlQKnCiR_NfoMp6IKLgR0sc5Ii6xlw/sendMessage", Nil, entity)
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
