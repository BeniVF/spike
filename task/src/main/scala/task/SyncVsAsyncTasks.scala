package task

import scalaz.concurrent.Task

object SyncVsAsyncTasks extends App {
  val start = init()

  val blockingTask = Task {
    work("Blocking", 10, start)
  }

  status("Waiting for future", start)
  val asyncTask = Task {
    work("Async", 10, start)
  }

  asyncTask.runAsync { result => status("Press Enter to continue", start) }

  blockingTask.run
  end(start)
}
