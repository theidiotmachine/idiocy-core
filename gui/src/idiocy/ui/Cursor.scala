package idiocy.ui

import java.awt.{Graphics, Point}

import idiocy.ui.renderer.PieceDisplayParams
import upickle.default.{ReadWriter => RW, macroRW}

object MeasureEventId{
  implicit def rw: RW[MeasureEventId] = macroRW
}

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

  def apply(sectionIds: Array[Int],
            staff: Int,
            measureId: Int, eventId: Int,
            barLine: Int = 0): Cursor = new Cursor(sectionIds, staff, MeasureEventId(measureId, eventId), barLine)

  def emptyCursor: Cursor = new Cursor(Array(), 0, MeasureEventId(0, 0), 0)
}

case class MeasureEventId(measureId: Int, eventId: Int) {
  def prevEvent: MeasureEventId = MeasureEventId(measureId, eventId - 1)
  def nextEvent: MeasureEventId = MeasureEventId(measureId, eventId + 1)
  def startOfNextMeasure: MeasureEventId = MeasureEventId(measureId + 1, 0)
}

case class Cursor(sectionIds: Array[Int],
                  staff: Int,
                  measureEventId: MeasureEventId,
                  barLine: Int) {

  def measureId: Int = measureEventId.measureId

  def startOfNextMeasure: Cursor = Cursor(sectionIds, staff, measureEventId.startOfNextMeasure, barLine)


  def render(graphics: Graphics, at: Point, pieceDisplayParams: PieceDisplayParams):  Unit = {
    graphics.setColor(GlobalUISettings.palette.cursorColor)
    graphics.drawLine(
      at.x, at.y - pieceDisplayParams.staffLineSeparationPixels / 2,
      at.x, at.y + pieceDisplayParams.staffLineSeparationPixels / 2)
  }

  def upABarLine: Cursor = Cursor(sectionIds, staff, measureEventId, barLine + 1)
  def downABarLine: Cursor = Cursor(sectionIds, staff, measureEventId, barLine - 1)

  def prevEvent: Cursor = Cursor(sectionIds, staff, measureEventId.prevEvent, barLine)
  def nextEvent: Cursor = Cursor(sectionIds, staff, measureEventId.nextEvent, barLine)

}
