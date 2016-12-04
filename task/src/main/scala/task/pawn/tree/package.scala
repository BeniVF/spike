package task.pawn

import scalaz.concurrent.Task

package object tree {

  import scalaz._
  import Scalaz._
  // Maybe use scalaz.Tree
  trait Tree[+A]

  case class Node[A](root: A, nodes: List[Tree[A]]) extends Tree[A]

  case object EmptyTree extends Tree[Nothing]

  def show[A](tree: Tree[A]): Task[String] = {
    def go[A](tree: Tree[A], spaces: String): Task[String] = tree match {
      case EmptyTree => Task.now("")
      case Node(root, nodes) =>
        nodes.map(node => go(node, s"$spaces ")).sequenceU.map(subTrees =>
          s"$spaces$root\n${subTrees.mkString("")}"
        )
    }
    go(tree, "")
  }
}
