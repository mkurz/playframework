/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "secret-sample",
    version := "1.0-SNAPSHOT",
    scalaVersion := (sys.props("scala.crossversions").split(" ").toSeq.filter(v => SemanticSelector(sys.props("scala.version")).matches(VersionNumber(v))) match {
      case Nil => sys.error("Unable to detect scalaVersion! Did you pass scala.crossversions and scala.version Java properties?")
      case Seq(version) => version
      case multiple => sys.error(s"Multiple crossScalaVersions matched query '${sys.props("scala.version")}': ${multiple.mkString(", ")}")
    }),
    updateOptions := updateOptions.value.withLatestSnapshots(false),
    update / evictionWarningOptions ~= (_.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false)),
    libraryDependencies += guice,
    TaskKey[Unit]("checkSecret") := {
      val file = IO.read(baseDirectory.value / "conf/application.conf")
      val Secret = """(?s).*play.http.secret.key="(.*)".*""".r
      file match {
        case Secret("changeme") => sys.error(s"secret not changed!!\n$file")
        case Secret(_)          =>
        case _                  => sys.error(s"secret not found!!\n$file")
      }
    }
  )
