package idiocy.ui.renderer

import idiocy.music.key.Key
import idiocy.ui.clipboard.ClipboardEvent

object DisplayEvent{
  val PipsToABeat = 144 //there are 144 pips to a quarter note

  val Whole: Int = PipsToABeat * 4
  val DottedHalf: Int = (PipsToABeat * 2) + PipsToABeat
  val Half: Int = PipsToABeat * 2
  val DottedQuarter: Int = PipsToABeat + PipsToABeat / 2
  val Quarter: Int = PipsToABeat
  val DottedEighth: Int = PipsToABeat / 2 + PipsToABeat / 4
  val Eighth: Int = PipsToABeat / 2
  val DottedSixteenth: Int = PipsToABeat / 4 + PipsToABeat / 8
  val Sixteenth: Int = PipsToABeat / 4

}

trait DisplayEvent {
  val lengthPips: Long
}
