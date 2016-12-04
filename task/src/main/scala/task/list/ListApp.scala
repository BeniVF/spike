package task.list

import scalaz.concurrent.Task


object ListApp extends App {

  import task.basic._

  private val value = 1000000

  val input = List.tabulate(value)(i => i)
  val recursiveResult = measure(Task {
    recursiveFoldRight(input)(0L)(_ + _)
  })
  val trampolineResult = measure(
    foldRight(input)(1L)(_  * _)
  )


  println(s"Recursive: $recursiveResult")
  println(s"Trampoline: $trampolineResult")


}
