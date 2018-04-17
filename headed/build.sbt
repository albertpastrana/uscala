import sbt.Keys._
import sbt._

name := "uscala-headed"
organization := "org.uscala"
description := """An ultra simple "list" that ensures the existence of at least an element (i.e. a non-empty list)."""
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
homepage := Some(url("https://github.com/albertpastrana/uscala/tree/master/headed"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)
scmInfo := Some(ScmInfo(browseUrl = new URL("https://github.com/albertpastrana/uscala/tree/master/headed"),
                        connection = "scm:git:git@github.com:albertpastrana/uscala.git"))

libraryDependencies ++= Seq(
  Dependencies.Specs2Core
)
