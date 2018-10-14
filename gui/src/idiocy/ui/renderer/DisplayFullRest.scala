package idiocy.ui.renderer

import idiocy.music.key.Key
import idiocy.ui.clipboard.{ClipboardEvent, ClipboardFullRest}
import upickle.default.{macroRW, ReadWriter => RW}

object DisplayFullRest{
  implicit def rw: RW[DisplayFullRest] = macroRW
}

final case class DisplayFullRest(lengthPips: Long,
                           barLocation: Int //just for rendering
                          ) extends DisplayRest {
}
