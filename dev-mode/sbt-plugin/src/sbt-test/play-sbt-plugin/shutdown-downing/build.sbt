import java.util.concurrent.TimeUnit
import sbt._

import sbt.Keys.libraryDependencies

//
// Copyright (C) Lightbend Inc. <https://www.lightbend.com>
//

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  // disable PlayLayoutPlugin because the `test` file used by `sbt-scripted` collides with the `test/` Play expects.
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    scalaVersion := (sys.props("scala.crossversions").split(" ").toSeq.filter(v => SemanticSelector(sys.props("scala.version")).matches(VersionNumber(v))) match {
      case Nil => sys.error("Unable to detect scalaVersion! Did you pass scala.crossversions and scala.version Java properties?")
      case Seq(version) => version
      case multiple => sys.error(s"Multiple crossScalaVersions matched query '${sys.props("scala.version")}': ${multiple.mkString(", ")}")
    }),
    updateOptions := updateOptions.value.withLatestSnapshots(false),
    update / evictionWarningOptions ~= (_.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false)),
    libraryDependencies += guice,
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    test / fork := true,
    PlayKeys.playInteractionMode := play.sbt.StaticPlayNonBlockingInteractionMode,
    commands += ScriptedTools.assertProcessIsStopped,
    InputKey[Unit]("awaitPidfileDeletion") := {
      val pidFile = target.value / "universal" / "stage" / "RUNNING_PID"
      // Use a polling loop of at most 30sec. Without it, the `scripted-test` moves on
      // before the application has finished to shut down
      val secs = 30
      // NiceToHave: replace with System.nanoTime()
      val end = System.currentTimeMillis() + secs * 1000
      while (pidFile.exists() && System.currentTimeMillis() < end) {
        TimeUnit.SECONDS.sleep(3)
      }
      if (pidFile.exists()) {
        println(s"[ERROR] RUNNING_PID file was not deleted in ${secs}s")
      } else {
        println("Application stopped.")
      }
    },
    InputKey[Unit]("verifyResourceContains") := {
      val args                         = Def.spaceDelimited("<path> <status> <words> ...").parsed
      val path :: status :: assertions = args
      ScriptedTools.verifyResourceContains(path, status.toInt, assertions)
    }
  )

//sigtermApplication
