package idiocy.ui.data

import idiocy.ui.renderer.DisplayEvent
import upickle.default.{ReadWriter => RW, macroRW}

object TimeSig{
  implicit def rw: RW[TimeSig] = macroRW
  def timeSig44 = new TimeSig(4, 4)
}

case class TimeSig(top: Int, bottom: Int) {
  def measureLengthPips: Long = DisplayEvent.PipsToABeat * 4 * top / bottom
}
