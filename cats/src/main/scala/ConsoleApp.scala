import cats.Monad
import cats.implicits._
import fs2.Task
import scala.io.StdIn
import fs2.interop.cats._

object ConsoleApp extends App {
  import core._
  implicit val S = fs2.Strategy.fromFixedDaemonPool(1, "console")

  object TaskConsole extends Console[Task] {
    def readLine: Task[String] = Task { StdIn.readLine }
    def writeLine(line: String): Task[Unit] = Task { println(line) }
  }

  implicit val x = TaskConsole
  val program: Task[Unit] = myMonadicProgram[Task]
  program.unsafeRun()
}

object core {
  trait Console[F[_]] {
    def readLine: F[String]
    def writeLine(line: String): F[Unit]
  }

  object Console {
    def readLine[F[_]](implicit console: Console[F]): F[String] = console.readLine
    def writeLine[F[_]](line: String)(implicit console: Console[F]): F[Unit] = console.writeLine(line)
  }

  import Console._
  def myMonadicProgram[F[_]: Console: Monad]: F[Unit] = for {
    _ <- writeLine("What is your name:")
    line <- readLine
    _ <- writeLine(s"Hello $line!")
  } yield ()
}

