//
// Copyright (C) Lightbend Inc. <https://www.lightbend.com>
//
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

namespaceReverseRouter := true

libraryDependencies ++= Seq(guice, specs2 % Test)

scalaVersion := (sys.props("scala.crossversions").split(" ").toSeq.filter(v => SemanticSelector(sys.props("scala.version")).matches(VersionNumber(v))) match {
  case Nil => sys.error("Unable to detect scalaVersion! Did you pass scala.crossversions and scala.version Java properties?")
  case Seq(version) => version
  case multiple => sys.error(s"Multiple crossScalaVersions matched query '${sys.props("scala.version")}': ${multiple.mkString(", ")}")
})
updateOptions := updateOptions.value.withLatestSnapshots(false)
update / evictionWarningOptions ~= (_.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false))

// can't use test directory since scripted calls its script "test"
Test / sourceDirectory := baseDirectory.value / "tests"

Test / scalaSource := baseDirectory.value / "tests"

// Generate a js router so we can test it with mocha
val generateJsRouter = TaskKey[Seq[File]]("generate-js-router")

generateJsRouter := {
  (Compile / runMain).toTask(" utils.JavaScriptRouterGenerator target/web/jsrouter/jsRoutes.js").value
  Seq(target.value / "web" / "jsrouter" / "jsRoutes.js")
}

TestAssets / resourceGenerators         += Def.task(generateJsRouter.value).taskValue
TestAssets / managedResourceDirectories += target.value / "web" / "jsrouter"

// We don't want source position mappers is this will make it very hard to debug
sourcePositionMappers := Nil

play.sbt.routes.RoutesKeys.routesImport := Nil
ScriptedTools.dumpRoutesSourceOnCompilationFailure

scalacOptions ++= {
  Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
  )
}
