package task.basic


import scalaz.concurrent.Task

object BasicTasks extends App {

  (Task.fail(new RuntimeException("Something")) or Task.now("Boom!")).run

  val otherTask = for {
    t1 <- Task.fork {
      Task(println("forked"))
    }
    t2 <- Task(println("now"))
  } yield ()

  otherTask.runAsync(_ => println("First run"))
  otherTask.runAsync(_ => println("Second run"))

  val tasks = for {
    _ <- Task {
      init()
    }
    t1 <- Task.fork {
      Task(doAJob("forked", 100))
    }
    t2 <- Task.now(doAJob("now", 50))
  } yield t1 + t2

  val value = tasks.run
  status(s"Sum: $value")

  end()

}


