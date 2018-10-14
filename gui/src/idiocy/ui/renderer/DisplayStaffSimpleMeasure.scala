package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.music.key.Key
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.data.TimeSig


class DisplayStaffSimpleMeasure(val timeSig: TimeSig, val bpm: Float, val key: Key,
                                val events: Array[DisplayEventSet]) extends DisplayMeasure {

  def insertNoteAtCursor(lengthPips: Int,
                         noteIdModifier: Int,
                         cursor: Cursor): (DisplayStaffSimpleMeasure, Cursor) = {
    ???
  }


  def render(graphics: Graphics,
             staffId: Int,
             measureId: Int,
             selection: Selection,
             offset: Point, bar0Offs: Int, canvas: Dimension, displayKey: Boolean,
             displayParams: PieceDisplayParams): Unit = {
    val noteSpace = events.length / canvas.width
    var xOffs = 0
    var i = 0
    while(i < events.length){
      var j = 0
      val eventsNow = events(i)
      while(j < eventsNow.length) {
        eventsNow(j) match {
          case displayNote: DisplayNote =>
            val yOffs = bar0Offs - (displayNote.barLocation / 2) * displayParams.staffLineSeparationPixels
            val stemUp = displayNote.barLocation < 0
            displayNote.render(graphics, new Point(offset.x + xOffs, offset.y + yOffs), stemUp, displayParams)
        }
        j += 1
      }
      xOffs += noteSpace
      i += 1
    }

    graphics.drawLine(offset.x + xOffs, offset.y, offset.x + xOffs, offset.y + canvas.height)
  }
}
