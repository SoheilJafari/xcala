package xcala.play.cross.models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.util.UUID

final case class ImageResizingRequest(
    id              : UUID,
    objectName      : String,
    bucketName      : String,
    targetWidth     : Int,
    fileOriginalName: String,
    resizedImageName: String,
    resultTopic     : String
) extends ImageResizingMessage {

  def makeSuccessfulResponse(): ImageResizingResponse =
    ImageResizingResponse(
      id    = id,
      error = None
    )

  def makeFailedResponse(errorMessage: String): ImageResizingResponse =
    ImageResizingResponse(
      id    = id,
      error = Some(errorMessage)
    )

}

object ImageResizingRequest {

  def create(
      objectName      : String,
      bucketName      : String,
      targetWidth     : Int,
      fileOriginalName: String,
      resizedImageName: String,
      resultTopic     : String
  ): ImageResizingRequest =
    ImageResizingRequest(
      id               = UUID.randomUUID(),
      objectName       = objectName,
      bucketName       = bucketName,
      targetWidth      = targetWidth,
      fileOriginalName = fileOriginalName,
      resizedImageName = resizedImageName,
      resultTopic      = resultTopic
    )

  implicit val formatter: OFormat[ImageResizingRequest] =
    Json.format[ImageResizingRequest]

}
