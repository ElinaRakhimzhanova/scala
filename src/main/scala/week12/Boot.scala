package week12

import java.io.InputStream
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import akka.util.Timeout
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object Boot extends App with SprayJsonSerializer {

  implicit val system: ActorSystem = ActorSystem("photo-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val timeout: Timeout = Timeout(10.seconds)

  val log = LoggerFactory.getLogger("Boot")
  val clientRegion: Regions = Regions.EU_CENTRAL_1
  val credentials = new BasicAWSCredentials("", "")
  val client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(clientRegion)
    .build()
  val bucketName = "my-photo-service-kbtu"

  createBucket(client, bucketName)

  val worker = system.actorOf(PhotoActor.props(client, bucketName))

  val route =
    path("health") {
      get {
        complete {
          "OK"
        }
      }
    } ~
      pathPrefix("photos") {
        concat(
          pathEndOrSingleSlash {
            concat(
              get {
                complete {
                  "all photos"
                }
              },
//              post {
//                extractRequestContext { ctx =>
//                  fileUpload("file") {
//                    case (metadata, byteSource) =>
//                      // Convert Source[ByteSring, Any] => InputStream
//                      val inputStream: InputStream = byteSource.runWith(
//                        StreamConverters.asInputStream(FiniteDuration(3, TimeUnit.SECONDS))
//                      )
//
//                      log.info(s"Content type: ${metadata.contentType}")
//                      log.info(s"Fieldname: ${metadata.fieldName}")
//                      log.info(s"Filename: ${metadata.fileName}")
//
//                      complete {
//                        (worker ? PhotoActor.UploadPhoto(inputStream, metadata.fileName, metadata.contentType.toString())).mapTo[Either[ErrorResponse, SuccessfulResponse]])
//                      }
//                  }
//                }
//              }
            )
          },
            path(Segment) { photo =>
              concat(
                get {
                  val future = (worker ? PhotoActor.DownloadPhoto(photo)).mapTo[Either[ErrorResponse, PhotoResponse]]

                  onSuccess(future) {
                    case Left(error) => complete(error.status, error.message)
                    case Right(photo) => complete(photo.status, HttpEntity(ContentType(`image/jpeg`), photo.message))
                  }
                }
              )
            }
        )
      }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  def createBucket(s3client: AmazonS3, bucket: String): Unit = {
    if (!s3client.doesBucketExistV2(bucket)) {
      s3client.createBucket(bucket)
      log.info(s"Bucket with name: $bucket created")
    } else {
      log.info(s"Bucket $bucket already exists")
      s3client.listBuckets().asScala.foreach(b => log.info(s"Bucket: ${b.getName}"))
    }
  }
}
