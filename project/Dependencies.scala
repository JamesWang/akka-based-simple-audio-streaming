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
    def akkaOf(artifact: String): ModuleID  = "com.typesafe.akka"       %%  artifact              % Versions.akka
    def akkaHttpOf(artifact: String=""): ModuleID  = "com.typesafe.akka"   %% s"akka-http$artifact"  % Versions.akkaHttp

    val akka      = akkaOf("akka-actor-typed")
    val akkaHttp  = akkaHttpOf("")
    val akkaJson  = akkaHttpOf("-spray-json")
    val akkaLog   = akkaOf("akka-slf4j")

    val cats      = "org.typelevel"      %% "cats-core"             % Versions.cats
    val logBack   = "ch.qos.logback"      % "logback-classic"       % Versions.logBack


    //tests
    val akkaTestkit  = akkaOf("akka-actor-testkit-typed") % Test
    val akkaHttpTest = akkaHttpOf("-testkit")             % Test

    val scalaTest    = "org.scalatest" %% "scalatest"       % Versions.scalaTest % Test
  }

  object CompilerPlugin {
  }
}
