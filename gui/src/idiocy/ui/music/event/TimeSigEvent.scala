package idiocy.ui.music.event

import idiocy.ui.data.TimeSig
import upickle.default.{macroRW, ReadWriter => RW}

object TimeSigEvent{
  implicit def rw: RW[TimeSigEvent] = macroRW
}

final case class TimeSigEvent(timeSig: TimeSig) extends MusicEvent {
  override val lengthPips: Int = 0
}
