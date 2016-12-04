package task.pawn.sys

import java.nio.file.Path

import scalaz.concurrent.Task

import scala.concurrent.duration._

object Zipper {

  def unzip(path: Path): Task[Unit] = executeProcess(s"unzip -o -d ${path.getParent.toString} ${path.toString}")().flatMap {
    _.fold(
      (exitCode, output) => Task.fail(new RuntimeException(s"Failure to list folder with exit code $exitCode: $output")),
      (output) => Task.now(()))
  }
}
