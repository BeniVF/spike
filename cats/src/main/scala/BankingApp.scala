import cats.data.NonEmptyList
import cats.free.Free
import cats.free.Inject
import cats.{Functor, Monad, ~>}
import cats.implicits._
import fs2.Task
import fs2.interop.cats._

import java.io.FileWriter

object BankingApp extends App {
  implicit val S = fs2.Strategy.fromFixedDaemonPool(1, "banking")
  type Amount = Int
  type Error = String
  type Account = String

  final case class From[A](value: A) extends AnyVal
  final case class To[A](value: A) extends AnyVal

  type TransferResult = Either[Error, (From[Account], To[Account])]

  trait Banking[F[_]] {
    def accounts: F[NonEmptyList[Account]]

    def balance(account: Account): F[Amount]

    def transfer(amount: Amount, from: From[Account], to: To[Account]): F[TransferResult]

    def withdraw(amount: Amount): F[Amount]
  }

  object Banking {
    def accounts[F[_]](implicit F: Banking[F]): F[NonEmptyList[Account]] = F.accounts

    def balance[F[_]](account: Account)(implicit F: Banking[F]): F[Amount] = F.balance(account)

    def transfer[F[_]](amount: Amount, from: From[Account], to: To[Account])(implicit F: Banking[F]): F[TransferResult] = F.transfer(amount, from, to)

    def withdraw[F[_]](amount: Amount)(implicit F: Banking[F]): F[Amount] = F.withdraw(amount)
  }

  sealed trait BankingF[A]

  case class Accounts[A](next: NonEmptyList[Account] => A) extends BankingF[A]
  case class Balance[A](account: Account, next: Amount => A) extends BankingF[A]
  case class Transfer[A](amount: Amount, from: From[Account], to: To[Account], next: TransferResult => A) extends BankingF[A]
  case class Withdraw[A](amount: Amount, next: Amount => A) extends BankingF[A]

  object BankingF {
    implicit val banking: Banking[BankingF] = new Banking[BankingF] {
      def accounts: BankingF[NonEmptyList[Account]] = Accounts(identity)
      def balance(account: Account): BankingF[Amount] = Balance(account, identity)
      def transfer(amount: Amount, from: From[Account], to: To[Account]): BankingF[TransferResult] =
        Transfer(amount, from, to, identity)
      def withdraw(amount: Amount): BankingF[Amount] = Withdraw(amount, identity)
    }
  }

  implicit def bankingFree[F[_]](implicit F: Banking[F]): Banking[Free[F, ?]] =
    new Banking[Free[F, ?]] {
      def accounts = Free.liftF(F.accounts)
      def balance(account: Account) = Free.liftF(F.balance(account))
      def transfer(amount: Amount, from: From[Account], to: To[Account]) = Free.liftF(F.transfer(amount, from, to))
      def withdraw(amount: Amount) = Free.liftF(F.withdraw(amount))
    }

  import Banking._

  def injectExample[F[_]](implicit inject: Inject[BankingF, F]): Free[F, Amount] =
    for {
      as <- Free.inject(accounts[BankingF])
      b <- Free.inject(balance[BankingF](as.head))
    } yield b

  def program[F[_]: Monad](implicit F: Banking[F]): F[Amount] =
    for {
      as <- F.accounts
      b <- F.balance(as.head)
      x <- F.transfer(123, From("Foo"), To("Bar"))
      _ <- F.withdraw(5)
    } yield b

  type Interpreter[F[_], G[_]] = F ~> Free[G, ?]
  type ~<[F[_], G[_]] = Interpreter[F, G]

  type Halt[F[_], A] = F[Unit]

  implicit def haltFunctor[F[_]]: Functor[Halt[F, ?]] = new Functor[Halt[F, ?]] {
    override def map[A, B](fa: Halt[F, A])(f: (A) => B): Halt[F, B] = fa
  }

  implicit class FreeHaltOps[F[_], A](free: Free[Halt[F, ?], A]) {
    def unhalt: Free[F, Unit] = free.fold(x => Free.pure(()), Free.liftF(_))
  }

  sealed trait LoggingF[A]

  object LoggingF {
    case class Log(string: String) extends LoggingF[Unit]
    def log(string: String): Free[LoggingF, Unit] = Free.liftF(Log(string))
  }

  sealed trait ProtocolF[A]

  object ProtocolF {
    case class JustReturn[A](a: A) extends ProtocolF[A]
  }

  sealed trait SocketF[A]

  object SocketF {
    case class JustReturn[A](a: A) extends SocketF[A]
  }

  sealed trait FileF[A]

  object FileF {
    case class AppendToFile(fileName: String, string: String) extends FileF[Unit]
    def appendToFile(fileName: String, string: String): Free[FileF, Unit] = Free.liftF(AppendToFile(fileName, string))
  }

  val bankingLogging: BankingF ~< Halt[LoggingF, ?] =
    new (BankingF ~< Halt[LoggingF, ?]) {
      override def apply[A](fa: BankingF[A]): Free[Halt[LoggingF, ?], A] = {
        def log(string: String): Free[Halt[LoggingF, ?], A] =
          Free.liftF[Halt[LoggingF, ?], A](LoggingF.Log(string))

        fa match {
          case Accounts(next) => log("Fetch accounts")
          case Balance(account, next) => log(s"Fetch balance for account = $account")
          case Transfer(amount, from, to, next) => log(s"Transfer [$amount] from $from to $to")
          case Withdraw(amount, next) => log(s"Withdraw $amount")
        }
      }
    }

  val loggingFile: LoggingF ~< FileF =
    new (LoggingF ~< FileF) {
      val fileName = "app.log"
      override def apply[A](fa: LoggingF[A]): Free[FileF, A] = fa match {
        case LoggingF.Log(string) =>
          FileF.appendToFile(fileName, string)
      }
    }

  val execFile: FileF ~> Task =
    new (FileF ~> Task) {
      override def apply[A](fa: FileF[A]): Task[A] =
        fa match {
          case FileF.AppendToFile(fileName, string) =>
            Task {
              val fw = new FileWriter(fileName, true)
              try {
                fw.write("\n" ++ string)
              } finally fw.close()
            }
        }
    }

  val bankingProtocol: BankingF ~< ProtocolF =
    new (BankingF ~< ProtocolF) {

      import ProtocolF._

      override def apply[A](fa: BankingF[A]) =
        fa match {
          case Accounts(next) => Free.liftF(JustReturn(next(NonEmptyList.of("Foo", "Bar"))))
          case Balance(account, next) => Free.liftF(JustReturn(next(10000)))
          case Transfer(amount, from, to, next) => Free.liftF(JustReturn(next(Left("Ooops"))))
          case Withdraw(amount, next) => Free.liftF(JustReturn(next(10000 - amount)))
        }
    }

  val protocolSocket: ProtocolF ~< SocketF =
    new (ProtocolF ~< SocketF) {
      override def apply[A](fa: ProtocolF[A]) =
        fa match {
          case ProtocolF.JustReturn(a) => Free.liftF(SocketF.JustReturn(a))
        }
    }

  val execSocket: SocketF ~> Task =
    new (SocketF ~> Task) {
      override def apply[A](fa: SocketF[A]): Task[A] =
        fa match {
          case SocketF.JustReturn(a) =>
            Task {
              println(a)
              a
            }
        }
    }

  val bankingFProgram = program[Free[BankingF, ?]]

  val execBanking = new (BankingF ~> Task) {
    override def apply[A](fa: BankingF[A]): Task[A] =
      for {
        _ <- bankingLogging(fa).unhalt.foldMap(loggingFile).foldMap(execFile)
        result <- bankingProtocol(fa).foldMap(protocolSocket).foldMap(execSocket)
      } yield result
  }

  val task = bankingFProgram.foldMap(execBanking)

  task.unsafeRun()

  
}

