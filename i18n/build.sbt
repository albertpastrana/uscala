import sbt._

name := "i18n"

description := "Simple message internationalization scala micro library with no dependencies for the JVM."

homepage := Some(url("https://github.com/albertpastrana/uscala/i18n"))

developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)

scmInfo := Some(
  ScmInfo(
    browseUrl = new URL("https://github.com/albertpastrana/uscala/i18n"),
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
  "org.specs2" %% "specs2-core" % "3.8.3" % "test"
)
