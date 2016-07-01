import sbt._

name := "uscala"
organization := "org.uscala"
description := "Set of general purpose micro libraries in scala."
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
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

releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value

scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild ++= Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-unchecked",
  "-deprecation",
  "-feature")

lazy val root = (project in file(".")).
  aggregate(i18n, result, resultSpecs2, resultAsync)

lazy val i18n = project

lazy val result = project

lazy val resultSpecs2 = (project in file("result-specs2")).dependsOn(result)

lazy val resultAsync = (project in file("result-async")).dependsOn(result)
