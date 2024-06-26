package services

import play.api.Configuration
import play.api.data.Form
import play.api.data.FormBinding
import play.api.mvc.AnyContent
import play.api.mvc.Request

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.nappin.play.recaptcha.RecaptchaVerifier

@Singleton
class RecaptchaService @Inject() (
    verifier     : RecaptchaVerifier,
    configuration: Configuration
) {

  val isRecaptchaEnabled: Boolean = configuration.get[Boolean]("isRecaptchaEnabled")

  def bindActiveFormBinder[T](form: Form[T])(implicit
      request: Request[AnyContent],
      ec     : ExecutionContext,
      binding: FormBinding
  ): Future[Form[T]] =
    isRecaptchaEnabled match {
      case true =>
        verifier.bindFromRequestAndVerify(form)

      case false =>
        Future.successful(form.bindFromRequest())
    }

}
