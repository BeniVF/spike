package task.basic

import java.util.concurrent.atomic.AtomicBoolean

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task


object CancellableTask extends App {

//  val neverMind = new AtomicBoolean(false)
//  Task {
//    Thread.sleep(500)
//    neverMind.set(true)
//  }.runAsync {
//    case -\/(t) => t.printStackTrace()
//    case \/-(()) => println("Cancelled")
//  }
//  val t = Task.delay {
//    Thread.sleep(1000)
//    TheNukes.launch()
//  }
//  t.runAsyncInterruptibly({
//    case -\/(t) => t.printStackTrace()
//    case \/-(()) => println("Completed")
//  }, neverMind)

  val anotherNeverMind = new AtomicBoolean(false)
  Task {
    Thread.sleep(500)
    anotherNeverMind.set(true)
  }.runAsync {
    case -\/(t) => t.printStackTrace()
    case \/-(()) => println("Cancelled")
  }

  val t2 = Task.delay {
    Thread.sleep(1000)
    TheNukes.launch()
  }
  t2.runAsyncInterruptibly({
    case -\/(t) => t.printStackTrace()
    case \/-(()) => println("Completed")
  }, anotherNeverMind)

}


object  TheNukes {
  def launch(): Unit = println("Boom!")
}