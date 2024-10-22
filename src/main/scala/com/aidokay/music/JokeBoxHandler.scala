package com.aidokay.music

import akka.NotUsed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.{ActorRef, Cancellable}
import akka.stream.scaladsl.{BroadcastHub, Keep, RunnableGraph, Source}
import akka.util.ByteString
import com.aidokay.music.MusicDownloader.TrackInfoProvider
import com.aidokay.music.JokeBox._
import com.aidokay.music.JokeBoxData.JokeBoxContext
import com.aidokay.music.NetController.typedSystem
import com.aidokay.music.tracks.AudioProvider

import scala.concurrent.duration.DurationInt

class JokeBoxHandler(audioProvider: AudioProvider[String]) {

  private val jokeBoxData = new JokeBoxContext(audioProvider)

  private def streamAudioChunk(): Iterator[ByteString] =
    new Iterator[ByteString] {
      override def hasNext: Boolean = true

      override def next(): ByteString = {
        if (jokeBoxData.state() == Paused) return ByteString.empty
        jokeBoxData.currentTrack() match {
          case None =>
            if (jokeBoxData.isEmpty) jokeBoxData.updateCurrentState(Paused)
            else jokeBoxData.playNext()
            ByteString.empty
          case Some(playing) =>
            if (playing.track.isDone && jokeBoxData.isEmpty) {
              jokeBoxData.stopPlaying()
              ByteString.empty
            } else {
              val chunk = playing.chunks.next()
              if (chunk.isEmpty) {
                playing.track.close()
                jokeBoxData.stopPlaying()
              }
              chunk
            }
        }
      }
    }

  def play()(using ctx: ActorContext[MusicBox]): Unit = {
    jokeBoxData.updateCurrentState(Playing)
  }
  def pause()(using ctx: ActorContext[MusicBox]): Unit = {
    jokeBoxData.updateCurrentState(Paused)
  }

  def list(replyTo: ActorRef)(using ctx: ActorContext[MusicBox]): Unit = {
    replyTo ! ListedMusic(audioProvider.audioList())
  }

  def schedule(tracks: List[String], replyTo: ActorRef)(
      using ctx: ActorContext[MusicBox]
  ): Unit = {
    if (tracks.contains("all")) {
      ctx.log.info("scheduling all found music")
      jokeBoxData.allAudios()
    } else {
      ctx.log.info(s"scheduling $tracks")
      tracks.foreach(jokeBoxData.offer)
    }
  }

  private val timedSource: Source[() => ByteString, Cancellable] =
    Source.tick(1.second, 200.millisecond, streamAudioChunk().next)

  private def runnableGraph(): RunnableGraph[Source[ByteString, NotUsed]] =
    timedSource.map(_())
      .toMat(
        BroadcastHub.sink[ByteString](bufferSize = 64)
      )(Keep.right)

  private val fromProducer: Source[ByteString, NotUsed] = runnableGraph().run()

  def apply(): Behavior[MusicBox] = {
    //TODO
    var streamerInstance: Option[Cancellable] = None
    Behaviors.setup { context =>

      given ActorContext[MusicBox] = context

      val musicDownloader = context.spawn(new TrackInfoProvider(audioProvider).apply(), "musicDownloader")
      Behaviors.receiveMessage { message =>
        context.log.info(s"Received: $message")
        message match {
          case ListMusic(replyTo) => list(replyTo)
          case PlayMusic(_) => play()
          case PauseMusic(_) => pause()
          case ScheduleMusic(tracks, replyTo) => schedule(tracks, replyTo)
          case Ignore => ()
          case SubscribeMusic(replyTo) =>
            context.log.info(s"SubscribeMusic from [$replyTo")
            replyTo ! Subscribed(fromProducer)
          case dld@DownloadMusic(_, _) =>
            musicDownloader ! dld
          case Cancel =>
            streamerInstance.foreach(_.cancel())
          case com.aidokay.music.JokeBox.ListedMusic(musics) => println(musics)
        }
        Behaviors.same
      }
    }
  }
}
