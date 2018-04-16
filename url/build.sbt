import sbt.Keys._
import sbt._

name := "uscala-url"
organization := "org.uscala"
description := "An immutable URL class with some useful methods to construct it, get the params, convert it to other types..."
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
homepage := Some(url("https://github.com/albertpastrana/uscala/tree/master/url"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)
scmInfo := Some(ScmInfo(browseUrl = new URL("https://github.com/albertpastrana/uscala/tree/master/url"),
                        connection = "scm:git:git@github.com:albertpastrana/uscala.git"))

libraryDependencies ++= Seq(
  Dependencies.Specs2Core,
  Dependencies.Specs2ScalaCheck
)
