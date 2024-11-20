package xcala.play.cross.services.s3

import xcala.play.cross.services.s3.FileStorageService.FileS3Object

import akka.NotUsed
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes
import akka.stream.Materializer
import akka.stream.alpakka.s3.MetaHeaders
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.ObjectMetadata
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString

import java.io.InputStream
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import io.sentry.Hint
import io.sentry.Sentry

class S3FileStorageService @Inject() (
    val config: play.api.Configuration
)(implicit val ec: ExecutionContext, materializer: Materializer) extends FileStorageService {

  override def upload(
      objectName  : String,
      content     : Array[Byte],
      contentType : String,
      originalName: String,
      path        : Option[String]
  ): Future[Boolean] = {
    val cleanPath: String = getCleanPath(path)

    val sink: Sink[ByteString, Future[MultipartUploadResult]] = S3
      .multipartUpload(
        bucket      = bucketName,
        key         = cleanPath.concat(objectName),
        metaHeaders = MetaHeaders(Map(elems = "name" -> originalName)),
        contentType =
          ContentType.parse(contentType).toOption.getOrElse(ContentTypes.`application/octet-stream`)
      )

    Source.single(ByteString(content))
      .runWith(sink)
      .transformWith {
        case Success(_) => Future.successful(true)
        case Failure(e) =>
          val hint = new Hint
          hint.set("objectName", objectName)
          Sentry.captureException(e, hint)
          Future.successful(false)
      }

  }

  override def findByObjectName(objectName: String, path: Option[String]): Future[FileS3Object] = {

    val cleanPath: String = getCleanPath(path)

    val s3Source: Source[ByteString, Future[ObjectMetadata]] = S3.getObject(
      bucket = bucketName,
      key    = cleanPath.concat(objectName)
    )

    val (metadataFuture: Future[ObjectMetadata], inputStream: InputStream) =
      s3Source.toMat(StreamConverters.asInputStream())(Keep.both).run()

    for {
      metadata   <- metadataFuture
      contentType = metadata.contentType
      originalName =
        metadata.metadata.find(_.name() == "X-Amz-Meta-Name").map(_.value()).getOrElse(objectName)
      contentLength = metadata.contentLength

    } yield FileS3Object(
      objectName    = objectName,
      originalName  = originalName,
      content       = inputStream,
      contentLength = Some(contentLength).filter(_ != 0),
      contentType   = contentType,
      path          = path
    )

  }.transformWith {
    case Success(value) =>
      Future.successful(value)
    case Failure(e) =>
      val hint = new Hint
      hint.set("objectName", objectName)
      Sentry.captureException(e, hint)
      Future.failed(e)
  }

  override def deleteByObjectName(objectName: String, path: Option[String]): Future[Boolean] =
    S3.deleteObject(
      bucket = bucketName,
      key    = objectName
    )
      .run()
      .transformWith {
        case Success(_) => Future.successful(true)
        case Failure(e) =>
          val hint = new Hint
          hint.set("objectName", objectName)
          Sentry.captureException(e, hint)
          Future.successful(false)
      }

  override def filesStream(path: Option[String]): Source[String, NotUsed] =
    S3
      .listBucket(
        bucket = bucketName,
        prefix = None
      )
      .map(_.key)

  override def getList(path: Option[String]): Future[List[String]] =
    filesStream(path)
      .toMat(Sink.seq)(Keep.right)
      .run()
      .map(_.toList)
      .recoverWith { case e: Throwable =>
        Sentry.captureException(e)
        Future.successful(List.empty[String])
      }

}
