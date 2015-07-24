scalaVersion := "2.11.6"


val scalazVersion = "7.1.3"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion
)
