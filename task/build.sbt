scalaVersion := "2.11.6"


val scalazVersion = "7.1.3"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "joda-time" % "joda-time" % "2.9.2",
  "com.ning" % "async-http-client" % "1.9.36",
  "oncue.journal" %% "core" % "2.2.1"

)

mainClass := Some("task.pawn.PawnApp")