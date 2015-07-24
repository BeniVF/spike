package task

import scalaz.concurrent.Task

object ParallelTask extends App {
  val numberOfTask = 100
  val batchSize = 10

  val start = init()
  val tasks = (1 to numberOfTask).map(i => Task(work(s"Job $i", numberOfTask - i + 1, start)))

  // Batch processing
  val result = tasks.grouped(batchSize).foldLeft(List.empty[Task[Int]]) {
    case (acc, current) =>
      status(s"Batch processing $current", start)
      val result = Task.gatherUnordered(current.toList).run
      val subJob = Task {
        status(s"Starting sum $result", start)
        val total = result.sum
        status(s"+$result = total", start)
        total
      }
      acc :+ subJob
  }

  // Calculate the total
  val total = Task.gatherUnordered(result).run.sum

  status(s"Total is ... $total", start)

  end(start)

}
