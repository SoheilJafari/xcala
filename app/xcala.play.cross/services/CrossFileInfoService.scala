package xcala.play.cross.services

import xcala.play.services.s3.FileStorageService

import scala.concurrent.Future

trait CrossFileInfoService[Id, FI] {

  val fileStorageService: FileStorageService

  def removeFile(id: Id): Future[Either[String, Int]]

  def upload(fileEntity: FI, content: Array[Byte]): Future[Either[String, Id]]

}
