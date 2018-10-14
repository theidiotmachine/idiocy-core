package idiocy.ui.renderer

import java.awt.{Graphics, Point}

import idiocy.ui.GlobalUISettings
import upickle.default.{macroRW, ReadWriter => RW}

object DisplaySpan{
  implicit def rw: RW[DisplaySpan] = macroRW
}

final case class DisplaySpan(lengthPips: Long, barLocation: Int, accidental: Int) extends DisplayEvent{
  def render(graphics: Graphics, spanPoint: Point, displayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.spanColor)
    graphics.drawLine(spanPoint.x - 4, spanPoint.y, spanPoint.x + 4, spanPoint.y)
  }
}
