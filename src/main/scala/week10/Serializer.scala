package week10

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait Serializer extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val messageFormat: RootJsonFormat[TelegramMessage] = jsonFormat2(TelegramMessage)
}
