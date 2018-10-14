package idiocy.ui.renderer
import java.awt.{Graphics, Point}

import idiocy.music.key.Key
import idiocy.ui.GlobalUISettings
import idiocy.ui.clipboard.ClipboardEvent
import upickle.default.{macroRW, ReadWriter => RW}

object DisplayNote{
  val NoAccidental = 0
  val SharpAccidental = 1
  val FlatAccidental = 2
  val NaturalAccidental = 3
  val DoubleFlatAccidental = 4
  val DoubleSharpAccidental = 5

  val StemLength = 12
  val BeamHeight = 2
  val BeamLength = 6

  implicit def rw: RW[DisplayNote] = macroRW
}

final case class DisplayNote(lengthPips: Long,
                       barLocation: Int,
                       accidental: Int
                      ) extends DisplayEvent{
  private [this] def renderStem(graphics: Graphics, point: Point, stemUp: Boolean, numBeams: Int): Unit = {
    if(stemUp){
      graphics.drawLine(point.x + 3, point.y + 2, point.x + 3, point.y - DisplayNote.StemLength)
      if(numBeams >= 1)
        graphics.fillRect(point.x + 3, point.y - DisplayNote.StemLength,
          DisplayNote.BeamLength, DisplayNote.BeamHeight)

      if(numBeams >= 2)
        graphics.fillRect(point.x + 3, point.y - DisplayNote.StemLength + (DisplayNote.BeamHeight + 2),
          DisplayNote.BeamLength, DisplayNote.BeamHeight)
    }
    else {
      graphics.drawLine(point.x - 2, point.y - 2, point.x - 2, point.y + DisplayNote.StemLength)

      if(numBeams >= 1)
        graphics.fillRect(point.x - 2 - DisplayNote.BeamLength, point.y + DisplayNote.StemLength + 1 - DisplayNote.BeamHeight,
          DisplayNote.BeamLength, DisplayNote.BeamHeight)

      if(numBeams >= 2)
        graphics.fillRect(point.x - 2 - DisplayNote.BeamLength, point.y + DisplayNote.StemLength - (2*DisplayNote.BeamHeight + 1),
          DisplayNote.BeamLength, DisplayNote.BeamHeight)
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
      case DisplayEvent.Whole =>
        renderNote(graphics, filled = false, point, displayParams)
      case DisplayEvent.DottedHalf =>
        renderNote(graphics, filled = false, point, displayParams)
        renderDot(graphics, point)
        renderStem(graphics, point, stemUp, 0)
      case DisplayEvent.Half =>
        renderNote(graphics, filled = false, point, displayParams)
        renderStem(graphics, point, stemUp, 0)
      case DisplayEvent.DottedQuarter =>
        renderStem(graphics, point, stemUp, 0)
        renderNote(graphics, filled = true, point, displayParams)
        renderDot(graphics, point)
      case DisplayEvent.Quarter =>
        renderStem(graphics, point, stemUp, 0)
        renderNote(graphics, filled = true, point, displayParams)
      case DisplayEvent.DottedEighth =>
        renderStem(graphics, point, stemUp, 1)
        renderNote(graphics, filled = true, point, displayParams)
        renderDot(graphics, point)
      case DisplayEvent.Eighth =>
        renderStem(graphics, point, stemUp, 1)
        renderNote(graphics, filled = true, point, displayParams)
      case DisplayEvent.DottedSixteenth =>
        renderStem(graphics, point, stemUp, 2)
        renderNote(graphics, filled = true, point, displayParams)
        renderDot(graphics, point)
      case DisplayEvent.Sixteenth =>
        renderStem(graphics, point, stemUp, 2)
        renderNote(graphics, filled = true, point, displayParams)
      case _ =>
        graphics.drawString("?", point.x, point.y)
    }
  }
}
