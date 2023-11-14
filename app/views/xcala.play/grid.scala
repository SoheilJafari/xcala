package views.html.xcala.play

import xcala.play.models._

import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import play.twirl.api.HtmlFormat

import reactivemongo.api.bson.BSONObjectID

object grid {

  def apply[A](paginated: Paginated[A], updateTarget: String = "")(
      columns: Col[A]*
  )(implicit messages: Messages): HtmlFormat.Appendable = {
    renderGrid(paginated = paginated, updateTarget = updateTarget, columns = columns, messages = messages)
  }

  def renderGrid[A](
      paginated      : Paginated[A],
      updateTarget   : String,
      columns        : Seq[Col[A]],
      messages       : Messages,
      rowAttributes  : Seq[A => (String, String)] = Nil,
      maybeCreateCall: Option[Call]               = None
  ): HtmlFormat.Appendable = {
    val rows =
      paginated.data.map { row: A =>
        columns.map { c =>
          (c, c.fieldMapper(row), c.cssClass(row))
        } -> rowAttributes.map(_(row))
      }
    views.html.xcala.play.gridView(columns, rows, paginated, updateTarget, maybeCreateCall)(messages)
  }

}

object gridWithPager {

  def apply[A](
      paginated      : Paginated[A],
      updateTarget   : String                     = "",
      rowAttributes  : Seq[A => (String, String)] = Nil,
      maybeCreateCall: Option[Call]               = None
  )(
      columns        : Col[A]*
  )(implicit messages: Messages): HtmlFormat.Appendable = {
    Html(
      grid
        .renderGrid(
          paginated       = paginated,
          updateTarget    = updateTarget,
          columns         = columns,
          messages        = messages,
          rowAttributes   = rowAttributes,
          maybeCreateCall = maybeCreateCall
        )
        .body + pager(paginated).body
    )
  }

}

object gridHeader {

  def apply(name: String, sortExpression: String, paginated: Paginated[_], updateTarget: String = "")(implicit
      messages: Messages
  ): Html = {
    val colLabel = messages(name)

    sortExpression match {
      case ""   => Html(s"$colLabel")
      case sort =>
        val url = paginated.sort(Some(sort)).toQueryString

        val link = s"<a href='?$url' data-ajax='true' data-ajax-update-target='$updateTarget'>$colLabel</a>"

        val icon = paginated.queryOptions.sortInfos
          .find(_.field == sortExpression)
          .map { sortInfo =>
            val sortAlt = if (sortInfo.direction == -1) { "-alt" }
            else { "" }
            s"&nbsp;<span class='glyphicon glyphicon glyphicon-sort-by-attributes$sortAlt'></span>"
          }
          .getOrElse {
            ""
          }

        Html(link + icon)
    }
  }

}
