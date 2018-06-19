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

scalaVersion in ThisBuild := "2.12.6"
crossScalaVersions in ThisBuild := Seq("2.11.12", "2.12.6", "2.13.0-M4")

// scalac 2.11 flags from https://tpolecat.github.io/2014/04/11/scalac-flags.html
lazy val scalacOptions211 = Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"     // 2.11 only
)

// scalac 2.12 flags from https://tpolecat.github.io/2017/04/25/scalac-flags.html
lazy val scalacOptions212 = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)

// scalac 2.13 flags, adapted from the 2.12 ones above, but with the flags that have been removed taken out. Next to
// each item is a link to the Scala commit that explains why it was removed.
lazy val scalacOptions213 = (scalacOptions212.toSet -- Set(
  "-encoding", "utf-8", // No longer supported, UTF-8 is assumed.
  "-Yno-adapted-args", // https://github.com/scala/scala/commit/a82a56ea5ba1ff0fb3b69c0963d4aec620ab68ad,
  "-Ypartial-unification" // https://github.com/scala/scala/commit/7d5e0b01c7f645e4f727f704a18f93ce5c69a9dd
)).toSeq

def scalacOptionsVersion(scalaVersion: String) = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, scalaMajor)) if scalaMajor == 13 => scalacOptions213
    case Some((2, scalaMajor)) if scalaMajor == 12 => scalacOptions212
    case _ => scalacOptions211
  }
}

scalacOptions in ThisBuild ++= scalacOptionsVersion(scalaVersion.value)

// Adds a `src/main/scala-2.13+` source directory for Scala 2.13 and newer
// and a `src/main/scala-2.13-` source directory for Scala version older than 2.13
val scala213Compat = Seq(
  unmanagedSourceDirectories in Compile += {
    val sourceDir = (sourceDirectory in Compile).value
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor == 13 => sourceDir / "scala-2.13+"
      case _ => sourceDir / "scala-2.13-"
    }
  }
)

lazy val root = (project in file("."))
  .settings(releaseIgnoreUntrackedFiles := true)
  .aggregate(headed, i18n, resources, result, `result-async`, `result-specs2`, retry, timeout, `try-ops`, `typed-env`, `url`)

lazy val headed = project.settings(scala213Compat)
lazy val i18n = project.settings(scala213Compat)
lazy val resources = project.settings(scala213Compat)
lazy val result = project.settings(scala213Compat)
lazy val `result-async` = project.settings(scala213Compat).dependsOn(result, `result-specs2` % "test->compile")
lazy val `result-specs2` = project.settings(scala213Compat).dependsOn(result)
lazy val retry = project.settings(scala213Compat)
lazy val timeout = project.settings(scala213Compat)
lazy val `try-ops` = project.settings(scala213Compat)
lazy val `typed-env` = project.settings(scala213Compat).dependsOn(`try-ops`)
lazy val `url` = project.settings(scala213Compat)
