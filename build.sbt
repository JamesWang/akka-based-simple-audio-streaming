import Dependencies.{CompilerPlugin, Libraries}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "SimpleMusicStreamer"
  )


libraryDependencies ++= Seq(

  Libraries.akka,
  Libraries.akkaHttp,
  Libraries.cats,
  Libraries.akkaJson,
  Libraries.akkaLog,
  Libraries.logBack,
  Libraries.akkaTestkit,
  Libraries.akkaHttpTest,
  Libraries.scalaTest

)