package xcala.play.controllers

import play.api.Logger
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import xcala.play.services._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import xcala.play.utils.WithExecutionContext

trait DataCudController[A] extends Controller with WithMainPageResults with WithFormBinding with WithComposableActions with WithExecutionContext {
  protected def cudService: DataReadService[A] with DataSaveService[A] with DataRemoveService

  def defaultForm: Form[A]

  def createView(form: Form[A])(implicit request: RequestType[_]): Future[Result]

  def editView(form: Form[A], model: A)(implicit request: RequestType[_]): Future[Result]

  def create = action.async { implicit request =>
    createView(defaultForm.bindFromRequest.discardingErrors)
  }

  def createPost = action.async {implicit request =>
    val filledFormFuture = bindForm(defaultForm)

    filledFormFuture flatMap { filledForm =>
      filledForm.fold(
        formWithErrors => {
          DataCudController.logger.debug("Form Error on Create: " + formWithErrors.errors)
          createView(formWithErrors)
        },
        model => {
          cudService.insert(model) map { objectId =>
            successfulResult(Messages("message.successfulSave"))
          } recoverWith {
            case throwable: Throwable => recoverSaveError(throwable, filledForm)
          }
        })
    }
  }

  def edit(id: BSONObjectID) = action.async {implicit request =>
    cudService.findById(id).flatMap { modelOption =>
      modelOption match {
        case Some(model) => editView(defaultForm.fill(model), model)
        case None => Future.successful(NotFound)
      }
    }
  } 
  
  def editPost(id: BSONObjectID) = action.async {implicit request =>
    cudService.findById(id) flatMap {
      case None => Future.successful(NotFound)
      case Some(model) =>
        val boundForm = defaultForm.fill(model)
        val filledFormFuture = bindForm(boundForm)

        filledFormFuture flatMap { filledForm =>
          filledForm.fold(
            formWithErrors => {
              DataCudController.logger.debug("Form Error on Edit: " + formWithErrors.errors)
              editView(formWithErrors, model)
            },
            model =>
              cudService.save(model).map { objectId =>
                successfulResult(Messages("message.successfulSave"))
              } recoverWith {
                case throwable: Throwable => recoverSaveError(throwable, filledForm)
              }
          )
        }
    }
  }

  protected def recoverSaveError(throwable: Throwable, filledForm: Form[A])(implicit request: RequestType[_]): Future[Result] = {
    createView(filledForm.withGlobalError(throwable.getMessage()))
  }

  def delete(id: BSONObjectID) = action.async {implicit request =>
    cudService.remove(id).map {
      case error if error.ok => successfulResult(Messages("message.successfulDelete"))
      case _ => NotFound
    }
  }
}

object DataCudController {
  private val logger = Logger("DataCudController")
}
