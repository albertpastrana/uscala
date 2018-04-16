import sbt.Keys._
import sbt._

name := "uscala-result-async"
organization := "org.uscala"
description := "A right biased union type that holds a value for an asynchronous/future successful computation or a value for a failed one."
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
homepage := Some(url("https://github.com/albertpastrana/uscala/tree/master/result-async"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com")),
  Developer(id = "janstenpickle", name = "Chris Jansen", email = "", url = new URL("http://nic-cage.xyz"))
)
scmInfo := Some(ScmInfo(browseUrl = new URL("https://github.com/albertpastrana/uscala/tree/master/result-async"),
                        connection = "scm:git:git@github.com:albertpastrana/uscala.git"))

libraryDependencies ++= Seq(
  Dependencies.Specs2Core,
  Dependencies.Specs2ScalaCheck
)
