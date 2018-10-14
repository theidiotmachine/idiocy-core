package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.music.key.Key
import idiocy.ui.{GlobalUISettings, Selection}
import idiocy.ui.data.TimeSig



trait DisplayMeasure {
  val timeSig: TimeSig
  val bpm: Float
  val key: Key
  val events: Array[DisplayEventSet]

  def render(graphics: Graphics,
             staffId: Int,
             measureId: Int,
             selection: Selection,
             offset: Point, bar0Offs: Int, canvas: Dimension, displayKey: Boolean,
             displayParams: PieceDisplayParams): Unit

  def lengthPips: Long = {
    events.foldLeft(0L)((b, es)=>b + es.lengthPips)
  }

  def renderBarLines(graphics: Graphics, offset: Point, canvas: Dimension, howMany: Int, displayParams: PieceDisplayParams): Int = {
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

  protected [this] def noteSpace(canvas: Dimension): Int =
    //math.min(
      canvas.width / (events.length + 1)
      //, canvas.width / 4)

  /*
  def convertNote(note: Note, clef: Clef, displayKey: Key): DisplayNote = {
    val converter = key.scale.getConverter(displayKey.scale)
    val (scaleNumber, noteIdModifier) = converter.convert(note.scaleNumber, note.noteIdModifier)
    val clefLocation = clef.getBarLocation(displayKey, scaleNumber, noteIdModifier, note.octave)
    new DisplayNote(note.lengthPips, clefLocation.barLocation, clefLocation.modifer)
  }*/
}
