package task.basic

import task._

object SyncAsyncTask extends App {

  import scalaz.concurrent.Task

    init()

    val blockingTask = Task {
      doAJob("Blocking", 10)
    }

    status("Waiting for future")
    val asyncTask = Task {
      doAJob("Async", 10)
    }

    asyncTask.runAsync { result => status("Press Enter to continue") }

    blockingTask.run
    end()


}
