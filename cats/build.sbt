
scalaVersion := "2.11.8"


resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.8.1",
  "co.fs2" %% "fs2-core" % "0.9.2",
  "co.fs2" %% "fs2-cats" % "0.2.0"
)

scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"//,
//    "-Ypartial-unification"
  ) ++ unusedWarnings
lazy val unusedWarnings = Seq(
    "-Ywarn-unused",
    "-Ywarn-unused-import"
    )
