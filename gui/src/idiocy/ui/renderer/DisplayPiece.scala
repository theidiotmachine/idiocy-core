package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui._
import idiocy.ui.clipboard.InternalClipboard

class DisplayPiece(val system: DisplaySystem) {
  def insertMeasures(staffId: Int, measureId: Int, numMeasures: Int): DisplayPiece = {
    val newSystem = system.insertMeasures(staffId, measureId, numMeasures)
    new DisplayPiece(newSystem)
  }

  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayPiece, Cursor) = {
    val (newSystem, newCursor) = system.insertMeasuresAtCursor(cursor, numMeasures)
    (new DisplayPiece(newSystem), newCursor)
  }

  def deleteMeasures(staffId: Int, measureId: Int, numMeasures: Int): DisplayPiece = {
    val newSystem = system.deleteMeasures(staffId, measureId, numMeasures)
    new DisplayPiece(newSystem)
  }

  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayPiece, Cursor) = {
    val (newSystem, newCursor) = system.deleteMeasuresAtCursor(cursor, numMeasures)
    (new DisplayPiece(newSystem), newCursor)
  }

  def pasteFromClipboardAtCursor(cursor: Cursor, internalClipboard: InternalClipboard): (DisplayPiece, Cursor) = {
    val (newSystem, newCursor) = system.pasteFromClipboardAtCursor(cursor, internalClipboard)
    (new DisplayPiece(newSystem), newCursor)
  }

  def pasteFromClipboardOverSelection(cursor: Cursor,
                                      selection: Selection,
                                      internalClipboard: InternalClipboard): (DisplayPiece, Cursor, Selection) = {
    selection match {
      case _: NoSelection =>
        (this, cursor, selection)
      case measureSelection: MeasureSelection =>
        val (newSystem, newCursor, newSelection) = system.pasteFromClipboardOverMeasureSelection(cursor, measureSelection, internalClipboard)
        (new DisplayPiece(newSystem), newCursor, newSelection)
      case eventSelection: EventSelection =>
        val (newSystem, newCursor, newSelection) = system.pasteFromClipboardOverEventSelection(cursor, eventSelection, internalClipboard)
        (new DisplayPiece(newSystem), newCursor, newSelection)
    }
  }

  def copyToClipboard(selection: Selection): InternalClipboard = {
    system.copyToClipboard(selection)
  }

  def isEmpty: Boolean = system.isEmpty

  def backspaceAtCursor(cursor: Cursor): (DisplayPiece, Cursor) = {
    val (newSystem, newCursor) = system.backspaceAtCursor(cursor)
    (new DisplayPiece(newSystem), newCursor)
  }

  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (DisplayPiece, Cursor) = {
    val (newSystem, newCursor) = system.insertRestAtCursor(lengthPips, cursor)
    (new DisplayPiece(newSystem), newCursor)
  }

  def cursorMove(cursor: Cursor, direction: Int, pieceDisplayParams: PieceDisplayParams): (Cursor, Selection) = {
    val newCursor = system.cursorMove(cursor,  direction, pieceDisplayParams)
    (newCursor, NoSelection())
  }

  def selectionMove(cursor: Cursor, selection: Selection, direction: Int, pieceDisplayParams: PieceDisplayParams): (Cursor, Selection) =
    system.selectionMove(cursor, selection, direction, pieceDisplayParams)

  def appendNewGrandStaff(trackId: Int): DisplayPiece = {
    new DisplayPiece(system.appendNewGrandStaff(trackId))
  }

  def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int,
                         cursor: Cursor): (DisplayPiece, Cursor) = {
    val (newSystem, newCursor) = system.insertNoteAtCursor(lengthPips, noteIdModifier, cursor)
    (new DisplayPiece(newSystem), newCursor)
  }

  def render(graphics: Graphics,
             canvas: Dimension,
             cursor: Cursor,
             selection: Selection,
             pieceDisplayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.bgColor)
    graphics.fillRect(0, 0, canvas.width, canvas.height)

    if(system.isEmpty){

    } else {
      val oneLineHeight = system.heightPixels(pieceDisplayParams)
      val numLines = ((canvas.height - (pieceDisplayParams.borderSizePix * 2)) / oneLineHeight) + 1
      val drawWidth = (canvas.width - (pieceDisplayParams.borderSizePix * 2)) - pieceDisplayParams.clefWidthPixels
      var found = false
      var numMeasuresPerLine = 4
      while (!found) {
        val pix = numMeasuresPerLine * (pieceDisplayParams.maxMeasureWidthPixels + 1) //1 is the line between staffs
        if (pix < drawWidth) {
          numMeasuresPerLine += 4
        } else {
          found = true
        }
      }

      pieceDisplayParams.numMeasuresPerLine = numMeasuresPerLine

      system.render(graphics, new Point(pieceDisplayParams.borderSizePix, pieceDisplayParams.borderSizePix),
        new Dimension(canvas.width - 2 * pieceDisplayParams.borderSizePix, canvas.height - 2 * pieceDisplayParams.borderSizePix),
        0, numMeasuresPerLine, numLines, cursor, selection, pieceDisplayParams)
    }
  }
}
