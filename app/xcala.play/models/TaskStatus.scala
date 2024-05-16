package models

import xcala.play.models.Options

object TaskStatus extends Options {
  type Type = String

  val MessagePrefix: String = "taskStatus."

  val Running : String = "running"
  val Finished: String = "finished"

  val all: Seq[String] = Seq(
    Running,
    Finished
  )

}
