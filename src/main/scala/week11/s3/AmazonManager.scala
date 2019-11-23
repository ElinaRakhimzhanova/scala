package week11.s3

import java.io.{BufferedReader, BufferedWriter, File, FileWriter, InputStreamReader}

import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ObjectMetadata, PutObjectRequest}


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

      if (client.doesObjectExist(bucketName, path)){
        getObject(path)
        sender() ! Response(200, "File is downloaded")
      } else
        sender() ! Response(400, "File is not downloaded")
    }

    case PostFile(path: String) => {
        createObject(path)
        sender() ! Response(200, "File is uploaded")
    }
  }

  def getObject(path: String) = {
    val fullObject = client.getObject(new GetObjectRequest(bucketName, path))
    val objectStream = fullObject.getObjectContent
    val reader = new BufferedReader(new InputStreamReader(objectStream));
    var str: String = reader.readLine()
    do {
      val writer = new BufferedWriter(new FileWriter(objectKey + path))
      writer.write(str)
      str = reader.readLine()
      if(str == null) writer.close()
    } while (str != null)
  }

  def createObject(path: String) {
    if (fileExistsLocally(path)) {
      val file = new File(objectKey + path)
      val request = new PutObjectRequest(bucketName, path, file)
      val metadata = new ObjectMetadata()

      metadata.setContentType("plain/text")
      metadata.addUserMetadata("user-type", "customer")

      request.setMetadata(metadata)
      client.putObject(request)
    }
  }

  def fileExistsLocally(path: String): Boolean = {
    val file = new File(objectKey + path)
    if (file.exists()) {
      log.info("File exists locally")
      return true
    }
    return false
  }
}
