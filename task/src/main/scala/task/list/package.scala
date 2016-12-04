package task

import scalaz.concurrent.Task

package object list {

  def foldRight[A, B](list: List[A])(z: B)(f: (A, B) => B): Task[B] =
    if (list.isEmpty)
      Task.now(z)
    else
      Task.suspend(foldRight(list.tail)(z)(f)).map(f(list.head, _))


  def recursiveFoldRight[A, B](list: List[A])(z: B)(f: (A, B) => B): B =
    if (list.isEmpty)
      z
    else
      f(list.head, recursiveFoldRight(list.tail)(z)(f))

}
