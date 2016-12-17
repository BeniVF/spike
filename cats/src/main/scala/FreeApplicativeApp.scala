import cats.free.FreeApplicative
import cats.free.FreeApplicative.lift

object FreeApplicativeApp extends App {
  import Validation._
  import cats.implicits._

  val prog: Validation[Boolean] = (size(5) |@| hasNumber).map { case (l, r) => l && r }

  import cats.arrow.FunctionK
  type FromString[A] = String => A

  val compiler = λ[FunctionK[ValidationOp, FromString]] { fa => str =>
    fa match {
      case Size(size) => str.size >= size
      case HasNumber => str.exists(c => ('0' to '9').contains(c))
    }
  }

  val validator = prog.foldMap[FromString](compiler)

  println(validator("1234"))
  println(validator("12345"))

  import cats.data.Kleisli
  import cats.implicits._
  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  // recall Kleisli[Future, String, A] is the same as String => Future[A]
  type ParValidator[A] = Kleisli[Future, String, A]

  val parCompiler = λ[FunctionK[ValidationOp, ParValidator]] { fa =>
    Kleisli { str =>
      fa match {
        case Size(size) => Future { str.size >= size }
        case HasNumber => Future { str.exists(c => "0123456789".contains(c)) }
      }
    }
  }

  val parValidation = prog.foldMap[ParValidator](parCompiler)

  println(Await.result(parValidation.run("1234"), 0.nanos))
  println(Await.result(parValidation.run("12345"), 0.nanos))

  import cats.data.Const
  type Log[A] = Const[List[String], A]

  val loggingCompiler = λ[FunctionK[ValidationOp, Log]] {
    case Size(size) => Const(List(s"size>=$size"))
    case HasNumber => Const(List("Has number"))
  }

  def logValidation[A](validation: Validation[A]): List[String] =
    validation.foldMap[Log](loggingCompiler).getConst


  println(logValidation(prog))
  println(logValidation(size(5) *> hasNumber *> size(10)))
  println(logValidation((size(3) |@| hasNumber).map(_ || _ )))


  //Another useful property Applicatives have over Monads is that given two Applicatives F[_] and G[_], their product type FG[A] = (F[A], G[A]) is also an Applicative
  import cats.data.Prod
  type ValidationAndLog[A] = Prod[ParValidator, Log, A]

  val prodCompiler = λ[FunctionK[ValidationOp, ValidationAndLog]] {
    case Size(size) =>
      val f : ParValidator[Boolean]  = Kleisli(str => Future { str.size >= size })
      val l : Log[Boolean] = Const(List(s"size>=$size"))
      Prod[ParValidator, Log, Boolean](f, l)
    case HasNumber =>
      val f : ParValidator[Boolean]  = Kleisli(str => Future { str.exists(c => "0123456789".contains(c)) })
      val l : Log[Boolean] = Const(List("Has number"))
      Prod[ParValidator, Log, Boolean](f, l)
  }

  val prodValidation = prog.foldMap[ValidationAndLog](prodCompiler)

//  prodValidation.


}

object Validation {
  sealed trait ValidationOp[A]
  case class Size(size: Int) extends ValidationOp[Boolean]
  case object HasNumber extends ValidationOp[Boolean]

  type Validation[A] = FreeApplicative[ValidationOp, A]

  def size(size: Int): Validation[Boolean] = lift(Size(size))
  val hasNumber: Validation[Boolean] = lift(HasNumber)
}

