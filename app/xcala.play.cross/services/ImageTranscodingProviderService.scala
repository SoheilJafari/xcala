package xcala.play.cross.services

import xcala.play.services.s3.FileStorageService

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Configuration

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class ImageTranscodingProviderService @Inject() (
    actorSystem       : ActorSystem,
    configuration     : Configuration,
    fileStorageService: FileStorageService
)(implicit val ec: ExecutionContext, val mat: Materializer) {

  lazy val transcodingService: ImageTranscodingService = {
    new ImageTranscodingService(actorSystem, configuration, fileStorageService)
  }

}
