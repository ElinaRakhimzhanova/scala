package project.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import libraryBot.TelegramMessage
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait Serializer extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val messageFormat: RootJsonFormat[TelegramMessage] = jsonFormat2(TelegramMessage)
}
