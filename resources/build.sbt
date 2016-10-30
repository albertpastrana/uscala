import sbt.Keys._
import sbt._

name := "uscala-resources"
organization := "org.uscala"
description := "A layer on top of Java resource loading to be able to access resources safely"
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
homepage := Some(url("https://github.com/albertpastrana/uscala/tree/master/resources"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)
scmInfo := Some(ScmInfo(browseUrl = new URL("https://github.com/albertpastrana/uscala/tree/master/url"),
                        connection = "scm:git:git@github.com:albertpastrana/uscala.git"))

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.8.4" % "test"
)
