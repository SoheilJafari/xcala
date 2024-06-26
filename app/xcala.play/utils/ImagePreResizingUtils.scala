package xcala.play.utils

import xcala.play.cross.models._
import xcala.play.models.ImageRenders
import xcala.play.services.s3.FileStorageService

import java.io.ByteArrayOutputStream
import java.io.InputStream
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.util.Failure
import scala.util.Success
import scala.util.Using

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.metadata.ImageMetadata
import com.sksamuel.scrimage.webp.WebpWriter
import io.sentry.Sentry
import reactivemongo.api.bson.BSONObjectID

object ImagePreResizingUtils {

  def removePreResizes[Id](
      preResizedImageHolder: CrossPreResizedImageHolder[Id]
  )(
      implicit
      fileStorageService   : FileStorageService,
      ec                   : ExecutionContext
  ): Future[_] = {
    preResizedImageHolder.maybeImageFileId match {
      case Some(imageFileId) =>
        Future.traverse(ImageRenders.ImageResizedRenderType.all) { allowedResize =>
          val resizedFileName: String = allowedResize.resizedObjectName(
            imageFileId match {
              case x: BSONObjectID => x.stringify
              case x => x.toString()
            }
          )
          fileStorageService.deleteByObjectName(resizedFileName).transformWith {
            case Failure(_) =>
              Future.successful(())

            case Success(_) =>
              Future.successful(())
          }
        }
      case _                 =>
        Future.successful(())
    }

  }

  def uploadPreResizesRaw[Id](
      imageFileId       : Id,
      fileContent       : InputStream,
      fileOriginalName  : String
  )(
      implicit
      fileStorageService: FileStorageService,
      ec                : ExecutionContext
  ): Future[_] =
    Future.fromTry(
      Using(fileContent) { fileContent =>
        val originalImage: ImmutableImage = ImmutableImage
          .loader()
          .fromStream(fileContent)

        Future.traverse(ImageRenders.ImageResizedRenderType.all) { allowedResize =>
          val resizedFileName: String = allowedResize.resizedObjectName(
            imageFileId match {
              case x: BSONObjectID => x.stringify
              case x => x.toString()
            }
          )

          val bos = new ByteArrayOutputStream()

          val byteArrayFuture: Future[Array[Byte]] =
            Future {
              blocking {
                val resizedImage: ImmutableImage = originalImage
                  .scaleToWidth(
                    allowedResize.overriddenWidth.toInt,
                    ScaleMethod.Progressive
                  )
                WebpWriter.MAX_LOSSLESS_COMPRESSION.withMultiThread.write(
                  resizedImage,
                  ImageMetadata.fromImage(resizedImage),
                  bos
                )
                bos.close()

                bos.toByteArray
              }
            }
          for {
            byteArray <- byteArrayFuture
            result    <- fileStorageService.upload(
              objectName   = resizedFileName,
              content      = byteArray,
              contentType  = "image/webp",
              originalName = fileOriginalName
            )
          } yield result

        }
      }
    ).flatten

  def uploadPreResizes[Id](
      preResizedImageHolder: CrossPreResizedImageHolder[Id]
  )(
      implicit
      fileStorageService   : FileStorageService,
      ec                   : ExecutionContext
  ): Future[Unit] = {
    preResizedImageHolder.maybeImageFileId match {
      case Some(imageFileId) =>
        fileStorageService.findByObjectName(
          imageFileId match {
            case x: BSONObjectID => x.stringify
            case x => x.toString()
          }
        ).map {
          file =>
            uploadPreResizesRaw(
              imageFileId      = imageFileId,
              fileContent      = file.content,
              fileOriginalName = file.originalName
            ).transformWith {
              case Failure(exception) =>
                Sentry.captureException(exception)
                Future.failed(exception)

              case Success(_) =>
                Future.successful(())

            }

        }
      case _                 => Future.successful(())
    }

  }

}
