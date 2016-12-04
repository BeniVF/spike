package task.pawn

import java.nio.file.Path
import java.util.concurrent.TimeoutException

import task.basic._

import scala.concurrent.duration._
import scalaz.Nondeterminism
import scalaz.concurrent.Task

object PawnApp extends App {

  import journal._
  import http._
  import tree._
  import sys.FileSystem._
  import sys.Zipper._

  val logger: Task[Logger] = Task.now(Logger("PawnApp"))

  def info(message: String): Task[Unit] = for {
    log <- logger
  } yield log.info(message)

  private def unzipFrom(uri: String)(to: Path): Task[Unit] = for {
    downloadTimeout <- Task.now(30.seconds)
    _ <- info(s"Downloading $to from $uri")
    _ <-download(uri)(to).timed(downloadTimeout).handleWith(handleTimeOutException("download", downloadTimeout))
    _ <- info(s"$to downloaded!")
    _ <- info(s"Unzip $to")
    _ <- unzip(to).retryAccumulating(Seq(1.second, 2.seconds))
    _ <- info(s"$to unzipped!")
  } yield ()


  private def handleTimeOutException[A](command: String, duration: Duration): PartialFunction[Throwable,Task[A]] = {
    case e: TimeoutException => for {
      msg <- Task.now(s"Unable to $command in $duration")
      _ <- info(msg)
      failure <- Task.fail(new TimeoutException(msg))
    } yield failure
  }

  // Program
  val firstSourceUri = "https://codeload.github.com/BeniVF/category-theory/zip/master"
  val secondSourceUri = "https://codeload.github.com/BeniVF/spike/zip/master"
  val job = for {
    _ <- info("Init PawnApp")
    _ <- info("Creating tmp folder")
    targetFolder <- path("./tmp")
    _ <- mkdir(targetFolder)
    pathFromTarget = pathFrom(targetFolder) _
    firstZipFile <- pathFromTarget("category-theory.zip")
    secondZipFile <- pathFromTarget("spike.zip")
    _ <- Nondeterminism[Task].both(unzipFrom(firstSourceUri)(firstZipFile), unzipFrom(secondSourceUri)(secondZipFile))
    treeValue <- tree(targetFolder).flatMap(show)
    _ <- info(treeValue)
  } yield treeValue

//  Side Effects
  val result = job.attemptRun.fold(
    _.printStackTrace(),
    identity
  )

  println((1 to 2).map { _ =>
    job.run
  }.forall(_ == result))

}
