package xcala.play.controllers

import play.api.i18n.I18nSupport
import play.api.i18n.Lang
import play.api.i18n.MessagesApi
import play.api.mvc._
import reactivemongo.api.bson._
import xcala.play.models._
import xcala.play.services._

import scala.concurrent.Future
import xcala.play.utils.WithExecutionContext

trait MultilangDataSingleViewController[A <: WithLang]
    extends Results
    with WithComposableActions
    with WithExecutionContext
    with I18nSupport {
  implicit val messagesApi: MessagesApi
  protected val readService: DataReadService[A]

  def singleView(model: A)(implicit request: RequestType[_]): Future[Result]

  def defaultNotFound(implicit request: RequestType[_]): Result

  def view(
      id: BSONObjectID
  ) = action.async { implicit request =>
    readService.findById(id).flatMap {
      case None => Future.successful(defaultNotFound)
      case Some(model) =>
        singleView(model).map(_.withLang(Lang(model.lang)))
    }
  }

}
