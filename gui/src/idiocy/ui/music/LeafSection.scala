package idiocy.ui.music
import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.clipboard.InternalClipboard
import idiocy.ui.{Cursor, GlobalUISettings, Selection}
import idiocy.ui.renderer.PieceDisplayParams

object LeafSection{
  val HeaderSpace = 18
}
final class LeafSection(val name: String, val system: MusicSystem) extends Section{
  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (LeafSection, Cursor) = {
    val (newSystem, newCursor) = system.insertRestAtCursor(lengthPips, cursor)
    (new LeafSection(name, newSystem), newCursor)
  }

  def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int, cursor: Cursor): (LeafSection, Cursor) = {
    val (newSystem, newCursor) = system.insertNoteAtCursor(lengthPips, noteIdModifier, cursor)
    (new LeafSection(name, newSystem), newCursor)
  }

  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (LeafSection, Cursor) = {
    val (newSystem, newCursor) = system.insertMeasuresAtCursor(cursor, numMeasures)
    (new LeafSection(name, newSystem), newCursor)
  }

  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (LeafSection, Cursor) = {
    val (newSystem, newCursor) = system.deleteMeasuresAtCursor(cursor, numMeasures)
    (new LeafSection(name, newSystem), newCursor)
  }

  def backspaceAtCursor(cursor: Cursor): (LeafSection, Cursor) = {
    val (newSystem, newCursor) = system.backspaceAtCursor(cursor)
    (new LeafSection(name, newSystem), newCursor)
  }

  def appendNewGrandStaff: LeafSection = {
    new LeafSection(name, system.appendNewGrandStaff)
  }

  override def replace(sectionIds: Array[Int], newLeafSection: LeafSection): Section = {
    newLeafSection
  }

  override def pixelHeight(numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Int = {
    val oneLineHeight = system.lineHeightPixels(displayParams)

    LeafSection.HeaderSpace + numMeasuresPerLine * oneLineHeight
  }

  override def render(graphics: Graphics,
                      offset: Point,
                      canvas: Dimension,
                      numMeasuresPerLine: Int,
                      cursor: Cursor,
                      selection: Selection,
                      displayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.noteColor)
    graphics.setFont(GlobalUISettings.sectionNameFont)
    graphics.drawString(name, offset.x, offset.y + 12)

    if(system.isEmpty){

    } else {
      val oneLineHeight = system.lineHeightPixels(displayParams)
      val numLines = (canvas.height / oneLineHeight) + 1

      system.render(graphics, new Point(offset.x, offset.y + LeafSection.HeaderSpace),
        new Dimension(canvas.width, canvas.height),
        0, numMeasuresPerLine, numLines, cursor, selection, displayParams)
    }
  }

  override def isEmpty: Boolean = system.isEmpty

  def copyToClipboard(selection: Selection): InternalClipboard = system.copyToClipboard(selection)

  override def cursorMove(path: Array[Int],
                          cursor: Cursor,
                          direction: Int,
                          numMeasuresPerLine: Int,
                          displayParams: PieceDisplayParams): Option[Cursor] = {
    Some(system.cursorMove(cursor, direction, numMeasuresPerLine, displayParams))
  }


  override def selectionMove(sectionIds: Array[Int],
                             cursor: Cursor,
                             selection: Selection,
                             direction: Int,
                             numMeasuresPerLine: Int,
                             displayParams: PieceDisplayParams): Option[(Cursor, Selection)] =
    Some(system.selectionMove(cursor, selection, direction, numMeasuresPerLine, displayParams))

  override def getLeftCursor(path: Array[Int], cursor: Cursor): Option[Cursor] =
    Some(system.staffs(cursor.staff).getLeftCursor(cursor.sectionIds, cursor.staff, 0, cursor.barLine))

  override def getRightCursor(path: Array[Int], cursor: Cursor): Option[Cursor] ={
    val staff = system.staffs(cursor.staff)
    Some(staff.getRightCursor(cursor.sectionIds, cursor.staff, staff.numMeasures - 1, cursor.barLine))
  }
}
