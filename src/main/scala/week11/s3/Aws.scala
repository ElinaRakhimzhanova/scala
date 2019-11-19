package week11.s3

import akka.actor.{ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import akka.pattern.ask
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

object Aws extends App with Serializer {

  implicit val system: ActorSystem = ActorSystem("book-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)

  val log = LoggerFactory.getLogger("Aws")
  val clientRegion = Regions.EU_CENTRAL_1
  val credentials = new BasicAWSCredentials("", "")
  val bucketName = "kbtu-library"
  val bucketNameforAll = "kbtu-library-for-all"
  val objectKey = "/"

  val client = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(clientRegion)
    .build();

  val amazonManager = system.actorOf(AmazonManager.props(client, bucketName, objectKey), "amazon-manager")
  //val amazonManagerForAll = system.actorOf(AmazonManagerForAll.props(), "amazon-manager-for-all")

  def createBucket() = {

    if (!client.doesBucketExistV2(bucketName)) {
      client.createBucket(bucketName)

      val location = client.getBucketLocation(bucketName)
      println(s"Bucket location: $location")
    }
  }
  createBucket()

  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
      pathPrefix("library") {
        path("file") {
          get {
            parameters('filename.as[String]) { path =>
              complete {
                log.info(s"path $path")
                (amazonManager ? AmazonManager.GetFile(path)).mapTo[Response]
              }
            }
          }
        } ~
          path("file") {
            post {
              entity(as[Body]) { body =>
                complete {
                  (amazonManager ? AmazonManager.PostFile(body.path)).mapTo[Response]
                }
              }
            }
          }
//          ~
//            path("out") {
//              get {
//                complete {
//                  amazonManagerForAll ? AmazonManagerForAll.GetFiles(path).mapTo[Response]
//                }
//              }
//            } ~
//              path("in") {
//                post {
//                  entity(as[Body]) { body =>
//                    complete {
//                      amazonManagerForAll ? AmazonManagerForAll.PostFiles(path).mapTo[Responce]
//                    }
//                  }
//                }
//              }
      }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

}