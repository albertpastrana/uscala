import sbt._

object Dependencies {

  object Versions {
    val Specs2 = "4.3.2"
  }

  // Libraries
  val Specs2Core = "org.specs2" %% "specs2-core" % Versions.Specs2
  val Specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % Versions.Specs2

}
