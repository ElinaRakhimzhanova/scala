package week12

import java.awt.image.BufferedImage
import java.io.{BufferedReader, BufferedWriter, ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream, FileWriter, InputStream, InputStreamReader}

import akka.actor.{Actor, ActorLogging, Props}
import cats.instances.byte
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata, PutObjectRequest, S3ObjectInputStream}
import javax.imageio.ImageIO

object PhotoActor {
  case class UploadPhoto(inputStream: InputStream, fileName: String, contentType: String)

  case class DownloadPhoto(file: String)

  def props(client: AmazonS3, bucketName: String) = Props(new PhotoActor(client, bucketName))
}

class PhotoActor(client: AmazonS3, bucketName: String) extends Actor with ActorLogging {
  import PhotoActor._

  override def receive: Receive = {
    case UploadPhoto(inputStream, fileName, contentType) =>
      val metadata = new ObjectMetadata()
      val key = s"photos/$fileName"
      metadata.setContentType(contentType)
      val request = new PutObjectRequest(bucketName, key, inputStream, metadata)
      val result = client.putObject(request)
      sender() ! SuccessfulResponse(201, s"File is uploaded")
      log.info("Successfully put object with filename: {} to AWS S3", fileName)
      context.stop(self)

    case DownloadPhoto(fileName) =>
      val key = s"photos/$fileName"
      val path = s"src/main/resources/"
      val fullObject = client.getObject(new GetObjectRequest(bucketName, key))
      val objectStream: S3ObjectInputStream = fullObject.getObjectContent

      // TODO: inputStream => Array[Byte]
      // sender() ! Response(array[Byte], content)
      val cp = fullObject.getObjectMetadata.getContentType

      val reader = new BufferedReader(new InputStreamReader(objectStream));
      var str: String = reader.readLine()
      do {
        val writer = new BufferedWriter(new FileWriter(path + key))
        writer.write(str)
        str = reader.readLine()
        if(str == null) writer.close()
      } while (str != null)
      sender() ! SuccessfulResponse(201, s"image : $fileName exists")

  }
}
