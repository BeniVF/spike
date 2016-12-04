package task.pawn.sys

import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeoutException

import scala.concurrent.duration._
import scalaz.concurrent.Task

object FileSystem {
  import task.pawn.tree._

  private def mkDirCommand(path: Path): String = s"mkdir -p ${path.toString}"

  def mkdir(path: Path): Task[Unit] = executeProcess(mkDirCommand(path))(10.seconds).flatMap(_.fold(
    handleFailure(mkDirCommand(path)),
    output => Task.now(()))
  )

  def lsCommand(path: Path): String = {
    s"ls ${path.toString}"
  }

  def listDirectory(path: Path): Task[List[Path]] = executeProcess(lsCommand(path))(10.seconds).flatMap(_.fold(
    handleFailure(lsCommand(path)),
    output => Task(output.map(path.resolve)))
  )

  def tree(path: Path): Task[Tree[Path]] = {
    import scalaz._
    import Scalaz._
    for {
      result <- if (path.toFile.isDirectory) {
        for {
          subPaths <- listDirectory(path)
          subTree <- subPaths.map(subPath => Task.suspend(tree(subPath))).sequenceU
        } yield Node(path, subTree)
      } else {
        Task.now(Node(path, List()))
      }
    } yield result
  }

  def path(value: String): Task[Path] = Task(Paths.get(value))

  def pathFrom(path: Path)(fileName: String): Task[Path] = Task(path.resolve(fileName))

  private def handleFailure[A](command: String): (Int, List[String]) => Task[A] =
    (exitCode, output) => Task.fail(
      new scala.RuntimeException(s"Failure when executing a $command with exit code $exitCode: $output"))
}
