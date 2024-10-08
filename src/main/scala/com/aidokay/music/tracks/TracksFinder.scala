package com.aidokay.music.tracks

import java.io.File

object TracksFinder {
  trait TrackLoader[F[_], T] {
    type O
    def load(loc: T): F[O]
  }

  trait MusicTrack[T] {
    def isMusic(t: T): Boolean
  }

  private class MusicFileFilter(extPattern: String) extends MusicTrack[String] {
    def isMusic(name: String): Boolean = name.endsWith(extPattern)
  }

  class TrackFileLoader(musicFile: MusicTrack[String]) extends TrackLoader[List, String] {
    override type O = String

    private def listOfFiles(loc: File): List[String] = {
      val files = loc.listFiles
      if (files != null){
          files.map(_.getName).filter(musicFile.isMusic).toList
      } else Nil
    }
    override def load(loc: String): List[O] = listOfFiles(new File(loc))
  }

  def audioFileFinder(ext: String) = new TrackFileLoader(new MusicFileFilter(ext))
}
