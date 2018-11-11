package idiocy.ui.data

import idiocy.ui.music.event.MusicEvent

import upickle.default.{macroRW, ReadWriter => RW}

object TimeSig{
  implicit def rw: RW[TimeSig] = macroRW
  val timeSig44 = TimeSig(4, 4)
}

case class TimeSig(top: Int, bottom: Int) {
  def measureLengthPips: Long = MusicEvent.PipsToABeat * 4 * top / bottom
}
