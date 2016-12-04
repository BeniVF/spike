package task.basic

import task._

import scalaz.Reducer
import scalaz.concurrent.Task

object ParallelListTask extends App {

  val batchSize = 20

  val numberOfTask = 5
  val tasks = (1 to numberOfTask).map(i => Task(doAJob(s"Job $i", numberOfTask - i + 1)))

  Task.gatherUnordered(tasks).run

  import Reducer.IntProductReducer

  Task.reduceUnordered(tasks).run


}



//  init()
//
//  val tasks = (1 to numberOfTask).map(i => Task(doAJob(s"Job $i", numberOfTask - i + 1)))
//
//  // Batch processing
//  val subTasks = tasks.grouped(batchSize).foldLeft(List.empty[Task[Int]]) {
//    case (acc: List[Task[Int]], current) =>
//      status(s"Batch processing $current")
//      val result: List[Int] = Task.gatherUnordered(current.toList).run
//      acc.+:(Task {
//        val total = result.sum
//        doAJob(s"Sum($result)", total)
//        total
//      })
//  }
//
// import Reducer.IntProductReducer
//
//  val total = Task.reduceUnordered(subTasks).run
//
//  status(s"Total is ... $total")
//
//  end()

