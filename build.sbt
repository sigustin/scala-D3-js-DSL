// Turn this project into a Scala.js project by importing these settings
enablePlugins(ScalaJSPlugin)
enablePlugins(ScalaJSBundlerPlugin)

name := "Example"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.6"

testFrameworks += new TestFramework("utest.runner.Framework")

libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    "com.lihaoyi" %%% "utest" % "0.6.3" % "test"
)

scalaJSUseMainModuleInitializer := true
mainClass := Some("example.ScalaJSExample")


npmDependencies in Compile ++= (
  "d3" -> "4.12.2" ::
    Nil
  )

useYarn := true

scalacOptions ++=
  "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-deprecation" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:_" ::
    // "-Xlint:_" ::
    // "-Ywarn-unused" ::
    "-P:scalajs:sjsDefinedByDefault" ::
    Nil