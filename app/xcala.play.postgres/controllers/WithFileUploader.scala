package xcala.play.postgres.controllers

import xcala.play.cross.controllers.CrossWithFileUploader
import xcala.play.cross.models._
import xcala.play.cross.services.CrossFileInfoService
import xcala.play.postgres.models.FileInfo

import play.api.libs.Files.TemporaryFile
import play.api.mvc._

import java.nio.file.Files.readAllBytes
import java.util.UUID
import scala.concurrent.Future

import org.apache.commons.io.FilenameUtils
import org.joda.time.DateTime

object WithFileUploader {
  val AutoUploadSuffix: String = "_autoupload"
}

trait WithFileUploader extends CrossWithFileUploader[UUID] {

  protected val fileInfoService: CrossFileInfoService[UUID, FileInfo]

  protected def handleObsoleteUploadedFiles(
      filesIds: Seq[String]
  ): Future[Seq[Either[String, Int]]] =
    Future.traverse(filesIds.map(UUID.fromString))(fileInfoService.removeFile)

  protected def saveFile[A](
      filePart        : MultipartFormData.FilePart[TemporaryFile],
      maybeOldModel   : Option[A],
      handlePreResizes: Boolean
  ): Future[Option[UUID]] = {
    val fileExtension = FilenameUtils.getExtension(filePart.filename)

    val fileInfo = FileInfo(
      name        = filePart.filename,
      fileName    = java.util.UUID.randomUUID().toString() + fileExtension,
      extension   = fileExtension,
      contentType = filePart.contentType.getOrElse("unknown"),
      length      = filePart.ref.path.toFile.length,
      createTime  = DateTime.now,
      folderId    = None,
      isHidden    = true
    )

    val fileContentByteArray = readAllBytes(filePart.ref.path)
    fileInfoService.upload(fileInfo, fileContentByteArray).flatMap {
      case Right(fileId) =>
        {
          if (handlePreResizes) {

            handleNewPreResizes(
              maybeOldModel  = maybeOldModel.asInstanceOf[Option[CrossPreResizedImageHolder[UUID]]],
              newFileId      = fileId,
              newFileContent = fileContentByteArray,
              newFileName    = fileInfo.name
            )

          } else {
            Future.successful(())
          }
        }.map { _ =>
          Some(fileId)
        }
      case _             =>
        Future.successful(None)
    }
  }

}
