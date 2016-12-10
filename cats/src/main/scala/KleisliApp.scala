import cats._
import cats.instances.all._
import cats.data.Kleisli
import cats.syntax.flatMap._

import language.higherKinds

//http://typelevel.org/cats/datatypes/kleisli.html
//Kleisli[F[_], A, B] is just a wrapper around the function A => F[B]
object Catnip {
  implicit class IdOp[A](val a: A) extends AnyVal {
    def some: Option[A] = Some(a)
  }
  def none[A]: Option[A] = None
}

import Catnip._

object BasicKleisliApp extends App {
  // Compose monads
  val f = Kleisli { (x: Int) => (x + 1).some }
  val g = Kleisli { (x: Int) => if (x >= 10) (x * 100).some else none[Int] }

  println(s"${4.some >>= (f compose g).run}")
  println(s"${10.some >>= (f compose g).run}")
}

object KleisliMonadApp extends App {
  def ask[M[+_]: Monad, T]: Kleisli[M, T, T] = Kleisli { x: T => Monad[M].pure(x) }

  def foo[M[+_]: Monad]: Kleisli[M, String, Int] = for {
    s <- ask[M, String]
    t <- ask[M, String]
  } yield (s ++ t).length

  println(foo[Option].run("foo"))
  println(foo[List].run("foo"))
  println("End!")
}

object KleisliConfigApp extends App {
  // Kleisli could help to build from config to an App:q
  case class DbConfig(url: String, user: String, pass: String)
  trait Db
  object Db {
    val fromDbConfig: Kleisli[Option, DbConfig, Db] = Kleisli { dbConfig =>
      if (dbConfig.url.nonEmpty) new Db {}.some else none[Db]
    }
  }

  case class ServiceConfig(addr: String, port: Int)
  trait Service
  object Service {
    val fromServiceConfig: Kleisli[Option, ServiceConfig, Service] = Kleisli { serviceConfig =>
      if (serviceConfig.addr.nonEmpty) new Service {}.some else none[Service]
    }
  }

  case class AppConfig(dbConfig: DbConfig, serviceConfig: ServiceConfig)
  class App(db: Db, service: Service) {
    override def toString: String = "App has been created!"
  }

  def appFromAppConfig: Kleisli[Option, AppConfig, App] =
    for {
      db <- Db.fromDbConfig.local[AppConfig](_.dbConfig)
      sv <- Service.fromServiceConfig.local[AppConfig](_.serviceConfig)
    } yield new App(db, sv)

  val noneApp = appFromAppConfig.run(AppConfig(DbConfig("", "", ""), ServiceConfig("", 9000)))

  println(noneApp)

  val app = appFromAppConfig.run(AppConfig(DbConfig("db", "", ""), ServiceConfig("service", 9000)))

  println(app)
}

