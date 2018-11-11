package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.{Cursor, GlobalUISettings}
import idiocy.ui.music.event.MusicEventSet
import idiocy.ui.renderer.PieceDisplayParams

trait Measure {
  val events: Array[MusicEventSet]
  val firstEventIndex: Int

  protected [this] def renderBarLines(graphics: Graphics,
                                      offset: Point,
                                      canvas: Dimension,
                                      howMany: Int,
                                      displayParams: PieceDisplayParams): Int = {
    var i = 0
    var yOffs = 0
    graphics.setColor(GlobalUISettings.palette.staffColor)
    while(i < howMany - 1){
      graphics.drawLine(offset.x, offset.y + yOffs, offset.x + canvas.width, offset.y + yOffs)
      yOffs += displayParams.staffLineSeparationPixels + displayParams.staffLineWidthPixels
      i += 1
    }
    graphics.drawLine(offset.x, offset.y + yOffs, offset.x + canvas.width, offset.y + yOffs)
    yOffs += displayParams.staffLineWidthPixels
    yOffs
  }

  def numEvents: Int = events.length

  protected [this] def noteSpace(canvas: Dimension): Int =  canvas.width / (numEvents + 1)

  def xEventLoc(eventIdx: Int, canvas: Dimension): Int = {
    noteSpace(canvas) * (eventIdx + 1)
  }

  def xCursorLoc(eventIdx: Int, canvas: Dimension): Int = {
    val ns = noteSpace(canvas)
    xEventLoc(eventIdx, canvas) - ns / 2
  }

  def renderInsertCursor(graphics: Graphics,
                         offset: Point,
                         bar0Offs: Int,
                         canvas: Dimension,
                         cursor: Cursor,
                         displayParams: PieceDisplayParams): Unit
}
