trait Calc[F[_]] {
  def lit(i: Int): F[Int]
  def add(l: F[Int], r: F[Int]): F[Int]
  def sqrt(i: F[Int]): F[Double]
}
import cats.Id
object IdCalc extends Calc[Id] {

  def lit(i: Int) = i
  def add(l: Id[Int], r: Id[Int]) = l + r
  def sqrt(i: Id[Int]) = Math.sqrt(i.toDouble)
}

import cats.data.Const
object Serializer extends Calc[Const[String, ?]] {
  def lit(i: Int): Const[String, Int] = Const(i.toString)
  def add(l: Const[String, Int], r: Const[String, Int]): Const[String, Int] =
    Const(s"(${l.getConst} + ${r.getConst}")
  def sqrt(i: Const[String, Int]): Const[String, Double] =
    Const(s"sqrt(${i.getConst})")
}

object CalcApp extends App {

  def program[F[_]](calc: Calc[F]): F[Double] = {
    import calc._
    sqrt(add(lit(1), lit(3)))
  }

  println(program(IdCalc))
  println(program(Serializer))
}


