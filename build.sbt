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
releaseCrossBuild in ThisBuild := true

scalaVersion in ThisBuild := "2.12.4"
crossScalaVersions := Seq("2.11.12", "2.12.4")

scalacOptions in ThisBuild ++= Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-unchecked",
  "-deprecation",
  "-feature")

lazy val root = (project in file("."))
  .settings(releaseIgnoreUntrackedFiles := true)
  .aggregate(headed, i18n, resources, result, `result-async`, `result-specs2`, retry, timeout, `url`)

lazy val headed = project
lazy val i18n = project
lazy val resources = project
lazy val result = project
lazy val `result-async` = project.dependsOn(result, `result-specs2` % "test->compile")
lazy val `result-specs2` = project.dependsOn(result)
lazy val retry = project
lazy val timeout = project
lazy val `url` = project
