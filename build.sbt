import sbt._

name := "uscala"

description := "Set of general purpose micro libraries in scala."

homepage := Some(url("https://github.com/albertpastrana/uscala"))

developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)

scmInfo := Some(
  ScmInfo(
    browseUrl = new URL("https://github.com/albertpastrana/uscala"),
    connection = "scm:git:git@github.com:albertpastrana/uscala.git"
  )
)

licenses += "MIT" -> url("https://opensource.org/licenses/MIT")

scalaVersion := "2.11.8"

lazy val root = (project in file(".")).
  aggregate(i18n, result)

lazy val i18n = project

lazy val result = project
