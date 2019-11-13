package week11

import java.io.{BufferedReader, File, InputStreamReader}

import akka.actor.{Actor, Props}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ObjectMetadata, PutObjectRequest}
import collection.JavaConverters._

object AmazonManager {

  case class GetFile(file: String)

  case class PostFile(file : String)

  def props(): Props = Props(new AmazonManager)

}

class AmazonManager extends Actor {

  import AmazonManager._

  val clientRegion = Regions.EU_CENTRAL_1

  val credentials = new BasicAWSCredentials("AKIAT7O5CSXRKY3AP6WY", "Gxxv0UB87RkqKMKkF1Qx9ntomB9Cx9P+5J1Dm7nX")

  val client = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(clientRegion)
    .build();

  val bucketName = "kbtu-library"

  def createBucket() = {

    if (!client.doesBucketExistV2(bucketName)) {
      client.createBucket(bucketName)

      val location = client.getBucketLocation(bucketName)
      println(s"Bucket location: $location")
    }
  }

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

  //listObjects()

  def receive: Receive = {

    case GetFile(objectKey: String) => {
      val fullObject = client.getObject(new GetObjectRequest(bucketName, objectKey));

      val objectStream = fullObject.getObjectContent

      val reader = new BufferedReader(new InputStreamReader(objectStream));

      var str: String = reader.readLine()
      do {
        println(str)
        str = reader.readLine()
      } while (str != null)
    }

    case PostFile(objectKey: String, filename: String) => {

      // Upload a file as a new object with ContentType and title specified.
      val request = new PutObjectRequest(bucketName, objectKey, new File(filename))

      val metadata = new ObjectMetadata()

      metadata.setContentType("plain/text")
      metadata.addUserMetadata("user-type", "customer")

      request.setMetadata(metadata)
      client.putObject(request)
    }
  }

}
