import sbt._

name := "result"

description := "A right biased union type that holds a value for a successful computation or a value for a failed one."

homepage := Some(url("https://github.com/albertpastrana/uscala/result"))

developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)

scmInfo := Some(
  ScmInfo(
    browseUrl = new URL("https://github.com/albertpastrana/uscala/result"),
    connection = "scm:git:git@github.com:albertpastrana/uscala.git"
  )
)

licenses += "MIT" -> url("https://opensource.org/licenses/MIT")

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-unchecked",
  "-deprecation",
  "-feature")

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.8.3" % "test",
  "org.specs2" %% "specs2-scalacheck" % "3.8.3" % "test"
)