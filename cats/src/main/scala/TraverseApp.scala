import scala.language.higherKinds
import cats.Applicative
import cats.implicits._
import cats.Id
import cats.kernel.Monoid
import cats.data.Const

object TraverseApp extends App {

  def traverse[F[_]: Applicative, A, B](as: List[A])(f: A => F[B]): F[List[B]] =
    as.foldRight(List.empty[B].pure[F]) {
      case (a, acc) =>
        f(a).product(acc).map {
          case (b, bs) => b :: bs
        }
    }

  def map[A, B](as: List[A])(f: A => B): List[B] =
    traverse[Id, A, B](as)(a => f(a))

  def foldMap[A, B: Monoid](as: List[A])(f: A => B): B =
    traverse(as)(a => Const[B, B](f(a))).getConst

  println(traverse(List(1, 2, 3, 4))(a => Option(a)))
  println(map(List(4, 3, 5))(_ + 10))
  println(foldMap(List(4, 3, 5))(a => a * 10))

}


