package week11.s3

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

trait Serializer {
  implicit val res1 = jsonFormat2(Response)
  implicit val res2: RootJsonFormat[Body] = jsonFormat1(Body)
}
