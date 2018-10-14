package idiocy.ui

import java.awt.{Graphics, Point}

import idiocy.ui.renderer.PieceDisplayParams
import upickle.default.{ReadWriter => RW, macroRW}

object Cursor{
  val LEFT = 0
  val RIGHT = 1
  val UP = 2
  val DOWN = 3
  val CONTROL_LEFT = 4
  val CONTROL_RIGHT = 5
  val CONTROL_UP = 6
  val CONTROL_DOWN = 7

  implicit def rw: RW[Cursor] = macroRW
}

case class Cursor(staff: Int = 0, measure: Int = 0, event: Int = 0, barLine: Int = 0) {
  def upABarLine: Cursor = Cursor(staff, measure, event, barLine + 1)
  def downABarLine: Cursor = Cursor(staff, measure, event, barLine - 1)

  def startOfNextMeasure: Cursor = Cursor(staff, measure + 1, 0, barLine)

  def nextEvent: Cursor = Cursor(staff, measure, event + 1, barLine)
  def prevEvent: Cursor = Cursor(staff, measure, event - 1, barLine)

  def render(graphics: Graphics, at: Point, pieceDisplayParams: PieceDisplayParams):  Unit = {
    graphics.setColor(GlobalUISettings.palette.cursorColor)
    graphics.drawLine(
      at.x, at.y - pieceDisplayParams.staffLineSeparationPixels / 2,
      at.x, at.y + pieceDisplayParams.staffLineSeparationPixels / 2)
  }
}
