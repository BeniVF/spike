package task

import scalaz.concurrent.Task

package object basic {

  //Side Effects functions

  private def currentThreadName(): String = {
    Thread.currentThread().getName
  }

  def init(): Unit = {
    status("Init the tasks")
  }

  def status(msg: String): Unit = {
    println(s"[${currentThreadName()}] $msg")
  }

  def doAJob(msg: String, value: Int): Int = {
    status(s"Started work: $msg")
    Thread.sleep(value)
    status(s"Finished work: $msg")
    value
  }

  def end(): Unit = {
    scala.io.StdIn.readLine()
    status("End")
  }

  def measure[A](task: Task[A]): (A, Long) ={
    val start = System.currentTimeMillis()
    val result = task.run
    val end = System.currentTimeMillis()
    (result, end-start)
  }

}
