package week11.s3

import spray.json.DefaultJsonProtocol._

trait Serializer {
  implicit val res1 = jsonFormat2(Response)
  implicit val res2 = jsonFormat1(Body)

}
