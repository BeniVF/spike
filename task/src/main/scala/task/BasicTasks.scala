package task

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

object BasicTasks extends App {
  val start = init()

  val sumTask = for {
    t1 <- Task.fork {
      status("forking", start)
      Task {
        work("forked", 100, start)
      }
    }
    t2 <- Task.suspend {
      status("suspending", start)
      Task {
        work("suspend", 10, start)
      }
    }
    t3 <- Task.delay {
      work("delay", 12, start)
    }
    t4 <- Task.now {
      work("now", 0, start)
    }
  } yield t1 + t2 + t3 + t4


//  sumTask.runAsync(_ => ())
  sumTask.attemptRun match {
    case \/-(value) => status(s"Sum: $value", start)
    case -\/(error) => status(error.getMessage, start)
  }

  end(start)

}


