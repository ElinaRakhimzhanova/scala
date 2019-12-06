package week12

import java.awt.image.BufferedImage
import java.io.{BufferedReader, BufferedWriter, ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream, FileWriter, InputStream, InputStreamReader}

import akka.actor.{Actor, ActorLogging, Props}
import cats.instances.{byte, stream}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata, PutObjectRequest, S3ObjectInputStream}
import com.amazonaws.util.IOUtils
import javax.imageio
import javax.imageio.{ImageIO, ImageReader}
import javax.imageio.stream.ImageInputStream
import week11.s3.Response

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
      val key = s"$fileName"
      metadata.setContentType(contentType)
      val request = new PutObjectRequest(bucketName, key, inputStream, metadata)
      val result = client.putObject(request)
      sender() ! SuccessfulResponse(201, s"File is uploaded")
      log.info("Successfully put object with filename: {} to AWS S3", fileName)
      context.stop(self)

    case DownloadPhoto(fileName) =>
      val key = s"$fileName"
      val fullObject: S3ObjectInputStream = client.getObject(new GetObjectRequest(bucketName, key)).getObjectContent
      val bytes: Array[Byte] = IOUtils.toByteArray(fullObject)

      log.info(s"Successfully found photo with filename: ${fileName}")
      sender() ! Right(PhotoResponse(200, bytes))

  }
}
