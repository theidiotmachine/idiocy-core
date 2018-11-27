package idiocy.ui.music.event
import upickle.default.{macroRW, ReadWriter => RW}

object PartialRest{
  implicit def rw: RW[PartialRest] = macroRW
}

final case class PartialRest(lengthPips: Int,
                             barLocation: Int
                             ) extends MusicRest {

}
