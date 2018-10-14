package idiocy.ui.renderer

import java.awt.{Graphics, Point}

import idiocy.ui.GlobalUISettings


trait DisplayRest extends DisplayEvent{
  val lengthPips: Long

  private[this] def renderDot(graphics: Graphics, point: Point): Unit = {
    graphics.fillRect(point.x + 7, point.y - 1, 3, 3)
  }

  def render(graphics: Graphics, point: Point, displayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.noteColor)
    graphics.setFont(GlobalUISettings.noteFont)
    val fontMetrics = graphics.getFontMetrics
    val fontHeight = fontMetrics.getHeight
    val fontAscent = fontMetrics.getAscent
    lengthPips match {
      case DisplayEvent.Whole =>
        graphics.fillRect(point.x - 5, point.y - displayParams.staffLineSeparationPixels, 10, 5)
      case DisplayEvent.DottedHalf =>
        renderDot(graphics, point)
        graphics.fillRect(point.x - 5, point.y - 4, 10, 5)
      case DisplayEvent.Half =>
        graphics.fillRect(point.x - 5, point.y - 4, 10, 5)
      case DisplayEvent.Quarter =>
        graphics.drawString("\uD834\uDD3D", point.x, point.y - fontHeight / 2 + fontAscent)
      case DisplayEvent.DottedQuarter =>
        graphics.drawString("\uD834\uDD3D", point.x, point.y - fontHeight / 2 + fontAscent)
        renderDot(graphics, point)
      case DisplayEvent.Eighth =>
        graphics.drawString("\uD834\uDD3E", point.x, point.y - fontHeight / 2 + fontAscent)
      case DisplayEvent.DottedEighth =>
        graphics.drawString("\uD834\uDD3E", point.x, point.y - fontHeight / 2 + fontAscent)
        renderDot(graphics, point)
      case DisplayEvent.DottedSixteenth =>
        graphics.drawString("\uD834\uDD3F", point.x, point.y - fontHeight / 2 + fontAscent)
        renderDot(graphics, point)
      case DisplayEvent.Sixteenth =>
        graphics.drawString("\uD834\uDD3F", point.x, point.y - fontHeight / 2 + fontAscent)
      case _ => graphics.drawString("?", point.x, point.y)
    }
  }
}
