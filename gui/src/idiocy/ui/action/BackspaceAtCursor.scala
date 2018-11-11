package idiocy.ui.action

import idiocy.ui.music.Piece
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.PieceDisplayParams

class BackspaceAtCursor() extends SimpleUndoRedoAction{
  override def applyInternal(piece: Piece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection): ActionResult = {
    val (pieceOut, newCursor) = piece.backspaceAtCursor(cursor)
    ActionResult(pieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Backspace"
}
