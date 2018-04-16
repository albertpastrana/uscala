import sbt._

name := "uscala-i18n"
organization := "org.uscala"
description := "Simple message internationalization scala micro library with no dependencies for the JVM."
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
homepage := Some(url("https://github.com/albertpastrana/uscala/tree/master/i18n"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)
scmInfo := Some(ScmInfo(browseUrl = new URL("https://github.com/albertpastrana/uscala/tree/master/i18n"),
                        connection = "scm:git:git@github.com:albertpastrana/uscala.git"))

libraryDependencies ++= Seq(
  Dependencies.Specs2Core
)
