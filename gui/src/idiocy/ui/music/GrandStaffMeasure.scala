package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui._
import idiocy.ui.data.TimeSig
import idiocy.ui.music.event._
import idiocy.ui.renderer.PieceDisplayParams

object GrandStaffMeasure{
  def barLineLoc(bar0Offs: Int, barLocation: Int, displayParams: PieceDisplayParams): Int = {
    bar0Offs - (barLocation * (displayParams.staffLineWidthPixels + displayParams.staffLineSeparationPixels) / 2)
  }
}

final case class GrandStaffMeasure(timeSig: TimeSig, events: Array[MusicEventSet], firstEventIndex: Int) extends Measure{

  private [this] def renderSelection(graphics: Graphics,
                                     staffId: Int,
                                     measureId: Int,
                                     selection: Selection,
                                     offset: Point,
                                     bar0Offs: Int,
                                     canvas: Dimension,
                                     displayParams: PieceDisplayParams): Unit = {
    selection match {
      case _: NoSelection =>
      case measureSelection: MeasureSelection =>
        if(measureSelection.staffT <= staffId && measureSelection.staffB >= staffId && measureSelection.measureIdL <= measureId &&
          measureSelection.measureIdR >= measureId) {
          graphics.setColor(GlobalUISettings.palette.selectionBgColor)
          graphics.fillRect(offset.x, offset.y, canvas.width, canvas.height)
        }
      case eventSelection: EventSelection =>
        if(eventSelection.staff == staffId){
          if(eventSelection.measureIdL <= measureId && eventSelection.measureIdR >= measureId){
            val l = if(eventSelection.measureIdL == measureId)
              xCursorLoc(eventSelection.meidL.eventId, canvas)
            else
              0
            val r = if(eventSelection.measureIdR == measureId)
              xCursorLoc(eventSelection.meidR.eventId, canvas)
            else
              canvas.width

            graphics.setColor(GlobalUISettings.palette.selectionBgColor)
            graphics.fillRect(offset.x + l, offset.y, r-l, canvas.height
            )
          }
        }
    }
  }

  def render(graphics: Graphics,
                      staffId: Int,
                      measureId: Int,
                      selection: Selection,
                      offset: Point, bar0Offs: Int, canvas: Dimension,
                      displayKey: Boolean, displayParams: PieceDisplayParams): Unit = {
    renderSelection(graphics, staffId, measureId, selection, offset, bar0Offs, canvas, displayParams)

    renderBarLines(graphics,
      new Point(offset.x, offset.y + GrandStaffMeasure.barLineLoc(bar0Offs, 10, displayParams)),
      canvas, 5, displayParams)
    renderBarLines(graphics,
      new Point(offset.x, offset.y + GrandStaffMeasure.barLineLoc(bar0Offs, -2, displayParams)),
      canvas, 5, displayParams)
    graphics.drawLine(offset.x + canvas.width - 1,
      offset.y + GrandStaffMeasure.barLineLoc(bar0Offs, 10, displayParams),
      offset.x + canvas.width - 1,
      offset.y + GrandStaffMeasure.barLineLoc(bar0Offs, -10, displayParams))

    renderEvents(graphics, offset, bar0Offs, canvas, displayParams)
    graphics.setColor(GlobalUISettings.palette.staffColor)

  }

  private def renderEvents(graphics: Graphics, offset: Point, bar0Offs: Int,
                           canvas: Dimension, displayParams: PieceDisplayParams): Unit = {
    var i = 0
    while (i < events.length) {
      var j = 0
      val thisEvents = events(i)
      while(j < thisEvents.length) {
        thisEvents(j) match {
          case musicNote: MusicNote =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = GrandStaffMeasure.barLineLoc(bar0Offs, musicNote.barLocation, displayParams)
            val notePoint = new Point(offset.x + xOffs, offset.y + yOffs)

            if (musicNote.barLocation == 0) {
              graphics.setColor(GlobalUISettings.palette.staffColor)
              graphics.drawLine(notePoint.x - 4, notePoint.y, notePoint.x + 4, notePoint.y)
            } else if (musicNote.barLocation > 11) {
              var i = 12
              graphics.setColor(GlobalUISettings.palette.staffColor)
              while (i <= musicNote.barLocation) {
                val y = offset.y + GrandStaffMeasure.barLineLoc(bar0Offs, i, displayParams)
                graphics.drawLine(notePoint.x - 4, y, notePoint.x + 4, y)
                i += 2
              }
            } else if (musicNote.barLocation < -11) {
              var i = -12
              graphics.setColor(GlobalUISettings.palette.staffColor)
              while (i >= musicNote.barLocation) {
                val y = offset.y + GrandStaffMeasure.barLineLoc(bar0Offs, i, displayParams)
                graphics.drawLine(notePoint.x - 4, y, notePoint.x + 4, y)
                i -= 2
              }
            }

            val stemUp = musicNote.barLocation < 6 && musicNote.barLocation > 0 ||
              musicNote.barLocation < -6
            musicNote.render(graphics, notePoint, stemUp, displayParams)
          case partialRest: PartialRest =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = GrandStaffMeasure.barLineLoc(bar0Offs, partialRest.barLocation, displayParams)
            val restPoint = new Point(offset.x + xOffs, offset.y + yOffs)
            partialRest.render(graphics, restPoint, displayParams)
          case fullRest: FullRest =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = GrandStaffMeasure.barLineLoc(bar0Offs, fullRest.barLocation, displayParams)
            val restPoint = new Point(offset.x + xOffs, offset.y + yOffs)
            fullRest.render(graphics, restPoint, displayParams)
          case musicNoteSpan: MusicNoteSpan =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = GrandStaffMeasure.barLineLoc(bar0Offs, musicNoteSpan.barLocation, displayParams)
            val spanPoint = new Point(offset.x + xOffs, offset.y + yOffs)
            musicNoteSpan.render(graphics, spanPoint, displayParams)
        }
        j += 1
      }
      i += 1
    }
  }

  override def renderInsertCursor(graphics: Graphics,
                                  offset: Point,
                                  bar0Offs: Int,
                                  canvas: Dimension,
                                  cursor: Cursor,
                                  displayParams: PieceDisplayParams): Unit = {
    val eventIdx = cursor.measureEventId.eventId
    val xLoc = xCursorLoc(eventIdx, canvas)
    val yLoc = GrandStaffMeasure.barLineLoc(bar0Offs, cursor.barLine, displayParams)

    cursor.render(graphics, new Point(offset.x + xLoc, offset.y + yLoc), displayParams)
  }
}
