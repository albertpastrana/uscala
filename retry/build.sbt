import sbt.Keys._
import sbt._

name := "uscala-retry"
organization := "org.uscala"
description := "A small utility that retries a computation until it is successful using a backoff algorithm (exponential backoff by default)."
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
homepage := Some(url("https://github.com/albertpastrana/uscala/tree/master/retry"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)
scmInfo := Some(ScmInfo(browseUrl = new URL("https://github.com/albertpastrana/uscala/tree/master/retry"),
                        connection = "scm:git:git@github.com:albertpastrana/uscala.git"))

libraryDependencies ++= Seq(
  Dependencies.Specs2Core,
  Dependencies.Specs2ScalaCheck
)
