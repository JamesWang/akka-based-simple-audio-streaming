package com.aidokay.music

import akka.NotUsed
import akka.actor.{ActorRef, Cancellable}
import akka.stream.scaladsl.Source
import akka.util.ByteString

import java.nio.file.{Path, Paths}

object JokeBox {
  case object Controller

  sealed trait JokeBoxState
  case object Playing extends JokeBoxState
  case object Paused extends JokeBoxState

  sealed trait MusicBox
  final case class ListMusic(replyTo: ActorRef) extends MusicBox
  final case class PlayMusic(replyTo: ActorRef) extends MusicBox
  final case class PauseMusic(replyTo: ActorRef) extends MusicBox
  final case class ScheduleMusic(tracks: List[String], replyTo: ActorRef) extends MusicBox
  final case class DownloadMusic(track: String, replyTo: akka.actor.typed.ActorRef[DownloadInfo]) extends MusicBox
  final case class ListedMusic(music: List[String]) extends MusicBox
  case object Ignore extends MusicBox
  case object Cancel extends MusicBox
  final case class SubscribeMusic(replyTo: akka.actor.typed.ActorRef[Subscribed]) extends MusicBox
  final case class Subscribed(musicSource: Source[ByteString, NotUsed])
  final case class TrackLocation(location: String)
  final case class DownloadInfo(trackLoc: TrackLocation, trackName: String) {
    val fullPath: Path = Paths.get(trackLoc.location, trackName)
  }


}
