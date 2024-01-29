package com.aidokay.music.tracks

import scala.language.implicitConversions


object MusicProviders {
  def musicProvider(loc: String, ext: String=".mp3"): AudioProvider[String] =
    new AudioProvider[String]() {
      override def audioList(): List[String] = TracksFinder.audioFileFinder(ext).load(loc)

      override val location: String = loc
    }
}
