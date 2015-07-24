package object task {
  private def currentThreadName: String = {
    Thread.currentThread().getName
  }

  def init(): Long = {
    val start = System.currentTimeMillis
    println(s"0: [$currentThreadName] Start ")
    start
  }

  def status(msg: String, start: Long): Unit = {
    println(s"${System.currentTimeMillis - start}: [$currentThreadName] $msg ")
  }

  def work(msg: String, value: Int, start: Long): Int = {
    status(s"Started $msg", start)
    Thread.sleep(value)
    status(s"Finished $msg", start)
    value
  }

  def end(start: Long) : Unit = {
    waitForEndOfLine()
    status("End", start)
  }
  
  private def waitForEndOfLine(): Unit = {
    scala.io.StdIn.readLine()
  }

}
