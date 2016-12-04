package task.basic


import scalaz.Nondeterminism
import scalaz.concurrent.Task

object NondeterminismTask  extends App {

  val result = Nondeterminism[Task].nmap5(
    Task{doAJob("h", 100); "h"},
    Task{doAJob("e", 50); "e"},
    Task{doAJob("l", 20); "l"},
    Task{doAJob("l", 10); "l"},
    Task{doAJob("o", 10); "o"}) { (first, second, third, fourth, fifth) =>
    first + second + third + fourth + fifth
  }

  println(result.run)


}
