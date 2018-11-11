package idiocy.ui.action

import idiocy.ui.music.Piece
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.{Cursor, Selection}

class InsertNoteAtCursor(val lengthPips: Int,
                         val noteIdModifier: Int) extends SimpleUndoRedoAction {

  override def applyInternal(piece: Piece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection
                            ): ActionResult = {
    val (pieceOut, newCursor) = piece.insertNoteAtCursor(
      lengthPips, noteIdModifier, cursor)
    ActionResult(pieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Insert Note"
}
