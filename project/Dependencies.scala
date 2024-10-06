import sbt.*

object Dependencies {

  object Versions {
    val akka              = "2.8.6"
    val akkaHttp          = "10.5.3"
    val logBack           = "1.5.6"
    val cats              = "2.12.0"

    //plugins
    val betterMonadicFor  = "0.3.1"
    val kindProjector     = "0.13.2"
    val semanticDB        = "4.5.8"

    //test
    val scalaTest = "3.2.19"
  }

  object Libraries {
    def akka(artifact: String): ModuleID  = "com.typesafe.akka"       %%  artifact              % Versions.akka
    def akkaHttp(artifact: String): ModuleID  = "com.typesafe.akka"   %% s"akka-http$artifact"  % Versions.akkaHttp

    val akka      = akka("akka-actor-typed")
    val akkaHttp  = akkaHttp("akka-http")
    val akkaJson  = akkaHttp("-spray-json")
    val akkaLog   = akka("akka-slf4j")

    val cats      = "org.typelevel"      %% "cats-core"             % Versions.cats
    val logBack   = "ch.qos.logback"      % "logback-classic"       % Versions.logBack


    //tests
    val akkaTestkit  = akka("akka-actor-testkit-typed") % Test
    val akkaHttpTest = akkaHttp("-testkit")             % Test

    val scalaTest    = "org.scalatest" %% "scalatest"       % Versions.scalaTest % Test
  }

  object CompilerPlugin {
    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
    )
    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % Versions.kindProjector cross CrossVersion.full
    )
    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % Versions.semanticDB cross CrossVersion.full
    )
  }
}
