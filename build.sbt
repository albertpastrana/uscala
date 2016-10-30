import sbt._

name := "uscala"
organization := "org.uscala"
description := "Set of general purpose micro libraries in scala."
licenses += "MIT" -> new URL("https://opensource.org/licenses/MIT")
homepage := Some(new URL("https://github.com/albertpastrana/uscala"))
developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://albertpastrana.com"))
)
scmInfo := Some(
  ScmInfo(
    browseUrl = new URL("https://github.com/albertpastrana/uscala"),
    connection = "scm:git:git@github.com:albertpastrana/uscala.git"
  )
)

releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value

scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild ++= Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-unchecked",
  "-deprecation",
  "-feature")

lazy val root = (project in file(".")).
  aggregate(i18n, resources, result, `result-async`, `result-specs2`, retry, timeout, `url`)

lazy val i18n = project

lazy val resources = project

lazy val result = project

lazy val `result-async` = project.dependsOn(result, `result-specs2` % "test->compile")

lazy val `result-specs2` = project.dependsOn(result)

lazy val retry = project

lazy val timeout = project

lazy val `url` = project
