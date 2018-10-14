package idiocy.ui.renderer

import upickle.default.{ReadWriter => RW, macroRW}

object DisplayPartialRest{
  implicit def rw: RW[DisplayPartialRest] = macroRW
}

final case class DisplayPartialRest(lengthPips: Long,
                              barLocation: Int
                             ) extends DisplayRest {

}
