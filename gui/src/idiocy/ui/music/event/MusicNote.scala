package idiocy.ui.music.event
import java.awt.{Graphics, Point}

import idiocy.ui.GlobalUISettings
import idiocy.ui.renderer.PieceDisplayParams
import upickle.default.{macroRW, ReadWriter => RW}

object MusicNote{
  val NoAccidental = 0
  val SharpAccidental = 1
  val FlatAccidental = 2
  val NaturalAccidental = 3
  val DoubleFlatAccidental = 4
  val DoubleSharpAccidental = 5

  val StemLength = 12
  val BeamHeight = 2
  val BeamLength = 6

  implicit def rw: RW[MusicNote] = macroRW
}

final case class MusicNote(lengthPips: Int,
                barLocation: Int,
                accidental: Int
               ) extends MusicEvent {
  private [this] def renderStem(graphics: Graphics, point: Point, stemUp: Boolean, numBeams: Int): Unit = {
    if(stemUp){
      graphics.drawLine(point.x + 3, point.y + 2, point.x + 3, point.y - MusicNote.StemLength)
      if(numBeams >= 1)
        graphics.fillRect(point.x + 3, point.y - MusicNote.StemLength,
          MusicNote.BeamLength, MusicNote.BeamHeight)

      if(numBeams >= 2)
        graphics.fillRect(point.x + 3, point.y - MusicNote.StemLength + (MusicNote.BeamHeight + 2),
          MusicNote.BeamLength, MusicNote.BeamHeight)
    }
    else {
      graphics.drawLine(point.x - 2, point.y - 2, point.x - 2, point.y + MusicNote.StemLength)

      if(numBeams >= 1)
        graphics.fillRect(point.x - 2 - MusicNote.BeamLength, point.y + MusicNote.StemLength + 1 - MusicNote.BeamHeight,
          MusicNote.BeamLength, MusicNote.BeamHeight)

      if(numBeams >= 2)
        graphics.fillRect(point.x - 2 - MusicNote.BeamLength, point.y + MusicNote.StemLength - (2*MusicNote.BeamHeight + 1),
          MusicNote.BeamLength, MusicNote.BeamHeight)
    }
  }

  private[this] def renderDot(graphics: Graphics, point: Point): Unit = {
    graphics.fillRect(point.x + 5, point.y - 1, 3, 3)
  }

  private [this] def renderNote(graphics: Graphics, filled: Boolean, point: Point, displayParams: PieceDisplayParams): Unit = {
    val noteSize = displayParams.staffLineSeparationPixels - 2
    val halfNoteSize = noteSize / 2
    graphics.drawRect(point.x - halfNoteSize, point.y - halfNoteSize, noteSize, noteSize)
    if(filled)
      graphics.fillRect(point.x - halfNoteSize, point.y - halfNoteSize, noteSize, noteSize)
  }

  def render(graphics: Graphics, point: Point, stemUp: Boolean, displayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.noteColor)
    lengthPips match {
      case MusicEvent.Whole =>
        renderNote(graphics, filled = false, point, displayParams)
      case MusicEvent.DottedHalf =>
        renderNote(graphics, filled = false, point, displayParams)
        renderDot(graphics, point)
        renderStem(graphics, point, stemUp, 0)
      case MusicEvent.Half =>
        renderNote(graphics, filled = false, point, displayParams)
        renderStem(graphics, point, stemUp, 0)
      case MusicEvent.DottedQuarter =>
        renderStem(graphics, point, stemUp, 0)
        renderNote(graphics, filled = true, point, displayParams)
        renderDot(graphics, point)
      case MusicEvent.Quarter =>
        renderStem(graphics, point, stemUp, 0)
        renderNote(graphics, filled = true, point, displayParams)
      case MusicEvent.DottedEighth =>
        renderStem(graphics, point, stemUp, 1)
        renderNote(graphics, filled = true, point, displayParams)
        renderDot(graphics, point)
      case MusicEvent.Eighth =>
        renderStem(graphics, point, stemUp, 1)
        renderNote(graphics, filled = true, point, displayParams)
      case MusicEvent.DottedSixteenth =>
        renderStem(graphics, point, stemUp, 2)
        renderNote(graphics, filled = true, point, displayParams)
        renderDot(graphics, point)
      case MusicEvent.Sixteenth =>
        renderStem(graphics, point, stemUp, 2)
        renderNote(graphics, filled = true, point, displayParams)
      case _ =>
        graphics.drawString("?", point.x, point.y)
    }
  }
}
