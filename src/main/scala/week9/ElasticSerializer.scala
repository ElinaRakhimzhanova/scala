package week9

import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import week6.SprayJsonSerializer
import spray.json._
import week6.model.Movie

import scala.util.Try

trait ElasticSerializer extends SprayJsonSerializer {

  // object -> JSON string
  implicit object MovieIndexable extends Indexable[Movie] {
    override def json(movie: Movie): String = movie.toJson.compactPrint
  }

  // JSON string -> object
  // parseJson is a Spray method
  implicit object MovieHitReader extends HitReader[Movie] {
    override def read(hit: Hit): Either[Throwable, Movie] = {
      Try {
        val jsonAst = hit.sourceAsString.parseJson
        jsonAst.convertTo[Movie]
      }.toEither
    }
  }
}
