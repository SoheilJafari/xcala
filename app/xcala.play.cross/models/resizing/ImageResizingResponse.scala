package xcala.play.cross.models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.util.UUID

case class ImageResizingResponse(
    id   : UUID,
    error: Option[String]
) extends ImageResizingMessage

object ImageResizingResponse {

  implicit val formatter: OFormat[ImageResizingResponse] =
    Json.format[ImageResizingResponse]

}
