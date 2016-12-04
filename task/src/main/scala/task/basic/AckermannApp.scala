package task.basic

import scala.concurrent.ExecutionContext


object AckermannApp extends App {
  //  (1 to 10).foreach { i =>
  //    println(i)
  //    show("ackermann", measure {
  //      ackermann(i, 4)
  //    })
  //    show(i, "ackermannF", measure {
  //      import scala.concurrent.ExecutionContext.Implicits.global
  //      ackermannF(i, 0).map(result => println(result))
  //    })
  //    show(i, "ackermannT", measure {
  //      ackermannT(i, 0).run
  //    })
  //    show(i, "ackermannO", measure {
  //      ackermannO(i, 0).run
  //    })

  (1 to 100).foreach { i =>
    show(i, "ackermannF", ackermannO(i, 0).run)
    show(i, "ackermannO", ackermannO(i, 0).run)
  }

  def show(step: Int, description: String, measure: => Long): Unit = {
    val time = measure
    println(s"$step;$description;$time")
  }

  def measure[T](f: => Unit): Long = {
    val start = System.currentTimeMillis()
    val result = f
    val end = System.currentTimeMillis()
    end - start
  }

  def ackermann(m: Int, n: Int): Int = (m, n) match {
    case (0, _) => n + 1
    case (m, 0) => ackermann(m - 1, 1)
    case (m, n) => ackermann(m - 1, ackermann(m, n - 1))
  }


  import scala.concurrent.Future


  def ackermannF(m: Int, n: Int)(implicit executionContext: ExecutionContext): Future[Int] = {
    (m, n) match {
      case (0, _) => Future(n + 1)
      case (m, 0) => Future(ackermannF(m - 1, 1)).flatMap(identity)
      case (m, n) => for {
        x <- Future(ackermannF(m, n - 1))
        y <- x
        z <- Future(ackermannF(m - 1, y))
        r <- z
      } yield r
    }
  }


  import scalaz.concurrent.Task
  import Task._

  def ackermannT(m: Int, n: Int): Task[Int] = {
    (m, n) match {
      case (0, _) => now(n + 1)
      case (m, 0) => suspend(ackermannT(m - 1, 1))
      case (m, n) =>
        suspend(ackermannT(m, n - 1)).flatMap { x =>
          suspend(ackermannT(m - 1, x))
        }
    }
  }

  val maxStack = 512

  import scalaz.concurrent.Task
  import Task._

  def ackermannO(m: Int, n: Int): Task[Int] = {
    def step(m: Int, n: Int, stack: Int): Task[Int] =
      if (stack >= maxStack)
        suspend(ackermannO(m, n))
      else go(m, n, stack + 1)
    def go(m: Int, n: Int, stack: Int): Task[Int] = {
      //      println(s"$m, $n, $stack")
      (m, n) match {
        case (0, _) => now(n + 1)
        case (m, 0) => step(m - 1, 1, stack)
        case (m, n) => for {
          internalRec <- step(m, n - 1, stack)
          result <- step(m - 1, internalRec, stack)
        } yield result
      }
    }
    go(m, n, 0)
  }
}