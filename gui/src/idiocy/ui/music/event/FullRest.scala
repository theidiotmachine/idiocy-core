package idiocy.ui.music.event

import upickle.default.{macroRW, ReadWriter => RW}

object FullRest{
  implicit def rw: RW[FullRest] = macroRW
}

final case class FullRest(lengthPips: Long,
                          barLocation: Int //just for rendering
                          ) extends MusicRest {
}
