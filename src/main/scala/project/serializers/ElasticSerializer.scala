package project.serializers

import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import project.model.Book
import spray.json._

import scala.util.Try

trait ElasticSerializer extends SprayJsonSerializer{

  implicit object BookIndexable extends Indexable[Book] {
    override def json(book: Book): String = book.toJson.compactPrint
  }

  implicit object BookHitReader extends HitReader[Book] {
    override def read(hit: Hit): Either[Throwable, Book] = {
      Try {
        val jsonAst = hit.sourceAsString.parseJson
        jsonAst.convertTo[Book]
      }.toEither
    }
  }

}
