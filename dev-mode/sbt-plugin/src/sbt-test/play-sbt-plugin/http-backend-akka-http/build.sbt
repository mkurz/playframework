name := """play-scala-seed"""
organization := "com.example"

version := "1.0-SNAPSHOT"

//
// Copyright (C) Lightbend Inc. <https://www.lightbend.com>
//

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  // disable PlayLayoutPlugin because the `test` file used by `sbt-scripted` collides with the `test/` Play expects.
  .disablePlugins(PlayLayoutPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")
scalaVersion := sys.props("scala.version")
updateOptions := updateOptions.value.withLatestSnapshots(false)
evictionWarningOptions in update ~= (_.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false))

libraryDependencies += guice
libraryDependencies += specs2
libraryDependencies += ws

// Tyrus is the reference implementation for Java Websocket API (JSR-356)
libraryDependencies += "org.glassfish.tyrus" % "tyrus-container-jdk-client" % "1.20" % Test
