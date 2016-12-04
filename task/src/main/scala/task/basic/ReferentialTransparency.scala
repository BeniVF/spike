package task.basic

import scala.concurrent.{Await, Future}
import scalaz.concurrent.Task
import scala.concurrent.duration._

object ReferentialTransparency extends App {

  def effect1 = for {
    x <- Task(println("Boom!"))
    y <- Task(println("Boom!"))
  } yield (x, y)


  val aEffect = Task(println("Boom!"))

  def effect2 = for {
    x <- aEffect
    y <- aEffect
  } yield (x, y)

  //Side effects
  println("Task")
  (1 to 100).foreach { _  =>
    println("effect1")
    effect1.run
    println("effect2")
    effect2.run
  }
}

object NoReferentialTransparency extends App {

  import scala.concurrent.ExecutionContext.Implicits.global
  def effect1 = for {
    x <- Future(println("Boom!"))
    y <- Future(println("Boom!"))
  } yield (x, y)

  lazy val aEffect = Future(println("Boom!"))

  def effect2 = for {
    x <- aEffect
    y <- aEffect
  } yield (x, y)

  println("Future")
  (1 to 100).foreach { _ =>
    println("effect1")
    Await.result(effect1, Duration.Inf)
    println("effect2")
    Await.result(effect2, Duration.Inf)
  }
}


//
//  val effect1 = {
//    val random = new Random(0L)
//    val task = Task(random.nextInt)
//    for {
//      a <- task
//      b <- task
//    } yield println(a, b)
//  }
//
//  val effect2 = {
//    for {
//      a <- Task(new Random(0L).nextInt)
//      b <- Task(new Random(0L).nextInt)
//    } yield println(a, b)
//  }
//
//  effect1.run
//  effect2.run

//  (1 to 1000).foreach { _ =>
//    if (!(effect1.run == effect2.run))
//    println("Not referential transparency")
//  }


