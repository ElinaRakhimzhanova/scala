package week11.s3

import java.io.{BufferedReader, BufferedWriter, File, FileWriter, InputStreamReader}

import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ObjectMetadata, PutObjectRequest}

import scala.collection.JavaConverters._

object AmazonManager {

  case class GetFile(file: String)

  case class PostFile(file : String)

  def props(client: AmazonS3, bucketName: String, objectKey: String): Props =
    Props(new AmazonManager(client: AmazonS3, bucketName: String, objectKey: String))

}

class AmazonManager(client: AmazonS3, bucketName: String, objectKey: String) extends Actor with ActorLogging {

  import AmazonManager._

  def receive: Receive = {

    case GetFile(path: String) => {

      if (client.doesObjectExist(bucketName, objectKey + path)){
        log.info(s"path $path and $objectKey")
        getObject(objectKey + path)
        log.info("File is downloaded")
        sender() ! Response(200, "File is downloaded")
      } else
        log.info("File is not downloaded")
        sender() ! Response(400, "File is not downloaded")
    }

    case PostFile(path: String) => {
        createObject(path)
        log.info("File is uploaded")
        sender() ! Response(200, "File is uploaded")
    }
  }

  def getObject(path: String) = {
    val fullObject = client.getObject(new GetObjectRequest(bucketName, path))
    log.info("File is received")
    val objectStream = fullObject.getObjectContent
    val reader = new BufferedReader(new InputStreamReader(objectStream));
    var str: String = reader.readLine()
    do {
      val writer = new BufferedWriter(new FileWriter(path))
      writer.write(str)
      println(str)
      str = reader.readLine()
      if(str == null) writer.close()
      log.info(s"line $str")
    } while (str != null)
  }

  def fileIsUploaded(bucketName: String, file: String) : Boolean = {
    if(client.doesObjectExist(bucketName, file)) {
      log.info("File exists in s3")
      return true
    } else {
      log.info("File does not exist in s3")
      return false
    }
  }
  //  getObject("level-2")

  def createObject(path: String) {
    // Upload a file as a new object with ContentType and title specified.
    val file = new File(objectKey + path)
    val request = new PutObjectRequest(bucketName, path, file)
    val metadata = new ObjectMetadata()

    metadata.setContentType("plain/text")
    metadata.addUserMetadata("user-type", "customer")

    request.setMetadata(metadata)
    client.putObject(request)
    log.info("File is created")
    fileIsUploaded(bucketName, path)

  }

  //  createObject("my/long/path/temp.txt", "./src/main/resources/hello-kbtu.txt")

  def listObjects() = {
    val req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2);
    //    val result: ListObjectsV2Result

    var flag = true

    while (flag) {
      val result = client.listObjectsV2(req)

      result.getObjectSummaries().asScala.toList.foreach { objectSummary =>
        printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
      }
      // If there are more than maxKeys keys in the bucket, get a continuation token
      // and list the next objects.
      val token = result.getNextContinuationToken()
      System.out.println("Next Continuation Token: " + token);
      req.setContinuationToken(token)

      if (!result.isTruncated) {
        flag = false
      }
    }
  }
}
