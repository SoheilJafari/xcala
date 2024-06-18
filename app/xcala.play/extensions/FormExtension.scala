package xcala.play.extensions

import play.api.data.Form

object FormExtension {

  val queryOptionFormKeys: Seq[String] = Seq("page", "size", "sort")

  implicit class ExtendedForm[A](form: Form[A]) {

    def isEmptyWithoutQueryOptions: Boolean =
      form
        .data
        .filter {
          case (key, _) if queryOptionFormKeys.contains(key) => false
          case _                                             => true
        }
        .isEmpty

  }

}
