package idiocy.ui.music.event

import java.awt.{Graphics, Point}

import idiocy.ui.GlobalUISettings
import idiocy.ui.renderer.PieceDisplayParams
import upickle.default.{macroRW, ReadWriter => RW}

object MusicNoteSpan{
  implicit def rw: RW[MusicNoteSpan] = macroRW
}


final case class MusicNoteSpan(lengthPips: Int, barLocation: Int, accidental: Int) extends MusicEvent{
  def render(graphics: Graphics, spanPoint: Point, displayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.spanColor)
    graphics.drawLine(spanPoint.x - 4, spanPoint.y, spanPoint.x + 4, spanPoint.y)
  }
}
