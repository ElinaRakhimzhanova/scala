package week11.s3

import java.io.{BufferedReader, File, InputStreamReader}
import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.services.s3.AmazonS3
import week11.s3.AmazonManagerForAll.{GetFiles, PostFiles}
import scala.collection.JavaConverters._
import com.amazonaws.services.s3.model.{ GetObjectRequest, ListObjectsRequest, ObjectListing, ListObjectsV2Request }

object AmazonManagerForAll {

  case class GetFiles()

  case class PostFiles()

  def props(client: AmazonS3, bucketName: String, objectKey: String): Props =
    Props(new AmazonManagerForAll(client: AmazonS3, bucketName: String, objectKey: String))

}

class AmazonManagerForAll(client: AmazonS3, bucketName: String, objectKey: String) extends Actor with ActorLogging {

  import AmazonManagerForAll._

  def receive: Receive = {

    case GetFiles() => {
      val listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
      val list: ObjectListing = client.listObjects(bucketName)
      list.getObjectSummaries().forEach(key => getObject(key.getKey()))
      sender() ! Response(200, "All files are downloaded")

    }

    case PostFiles() => {
      checkFiles(new File(objectKey + "/out"), "")
      sender() ! Response(200, "All files are uploaded")
    }
  }

  def checkFiles(file: File, path: String): Unit = {
    if (file.isFile()) {
      createObject(path.substring(1))
    } else {
      file.listFiles.foreach(file => checkFiles(file, path + "/" + file.getName()))
    }
  }

  def fileIsUploaded(path: String): Boolean = {
    return client.doesObjectExist(bucketName, path)
  }

  def getObject(uploadPath: String) {
    if (uploadPath.takeRight(1) == "/") {
      return
    }
    val downloadPath: String = objectKey + "in/" + uploadPath
    var dirPath: String = downloadPath.substring(0, downloadPath.lastIndexOf('/'))
    var newDir = new File(dirPath)
    newDir.mkdir()

    client.getObject(
      new GetObjectRequest(bucketName, uploadPath),
      new File(downloadPath))
    println(s"Downloaded from path: $uploadPath with path: $downloadPath")
  }

  def createObject(path: String): Boolean = {
    val filePath: String = objectKey + "out/" + path
    val file = new File(filePath)
    if (!file.exists()) {
      return false;
    }
    client.putObject(bucketName, path, file)
    println(s"Uploaded from path: $filePath with path: $path")
    return true;
  }
}
