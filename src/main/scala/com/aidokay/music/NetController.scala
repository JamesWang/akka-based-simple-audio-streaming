package com.aidokay.music

import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.ActorSystem
import akka.actor.{
  Actor,
  ActorLogging,
  ActorRef,
  Props,
  ActorSystem => ClassicAS
}
import akka.io.Tcp.{Received, Write}
import akka.util.ByteString
import com.aidokay.music.JokeBox._
import com.aidokay.music.tracks.AudioProvider
import com.typesafe.config.{Config, ConfigFactory}

import java.net.InetSocketAddress

object NetController extends App {

  private class Controller(connection: ActorRef, remote: InetSocketAddress)
      extends Actor
      with ActorLogging {

    context.watch(connection)

    private val cmdMapping: Map[String, ActorRef => MusicBox] = Map(
      "/list" -> ListMusic.apply,
      "/play" -> PlayMusic.apply,
      "/pause" -> PauseMusic.apply
    )
    private def tracks(str: String): List[String] =
      str.substring(10).split(",").toList

    private def createCommand(strCmd: String): MusicBox = {
      cmdMapping.get(strCmd.trim) match {
        case None if strCmd.startsWith("/schedule") =>
          val track = tracks(strCmd)
          println(s"Schedule[$track] command received")
          ScheduleMusic(track, context.self)
        case Some(cmd) =>
          println(s"[$cmd] command received")
          cmd.apply(context.self)
        case None =>
          println(s"Invalid command $strCmd")
          Ignore
      }
    }

    override def receive: Receive = { case Received(command) =>
      createCommand(command.utf8String.trim) match {
        case cmd @ ListMusic(_) =>
          jokeBoxHandler ! cmd
          context.become { case ListedMusic(music) =>
            println(s"listed music: $music")
            connection ! Write(ByteString.fromString(music.mkString("\n")))
            context.unbecome()
          }
        case cmd => jokeBoxHandler ! cmd
      }
    }
  }
  val config: Config = ConfigFactory.parseString("akka.loglevel = DEBUG")
  given system: ClassicAS = ClassicAS("MusicNetController", config)
  given typedSystem: ActorSystem[Nothing] = system.toTyped

  system.actorOf(
    Props(classOf[MusicManager], classOf[Controller]),
    "netController"
  )
  import com.aidokay.music.tracks.MusicProviders.musicProvider
  given audioProvider: AudioProvider[String] = musicProvider(
    "V:\\MusicPhotos\\music"
  )
  private val jokeBoxHandler =
    system.spawn(new JokeBoxHandler(audioProvider).apply(), "jokeBoxHandler")

  StreamHttpServer.startHttpServer(routes =
    new StreamingRoutes(jokeBoxHandler).streamRoutes
  )
}
