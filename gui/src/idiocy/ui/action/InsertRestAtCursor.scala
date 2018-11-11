package idiocy.ui.action

import idiocy.ui.music.Piece
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.{Cursor, Selection}

class InsertRestAtCursor(val lengthPips: Int) extends SimpleUndoRedoAction {
  override def applyInternal(piece: Piece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection
                            ): ActionResult = {
    val (newPiece, newCursor) = piece.insertRestAtCursor(lengthPips, cursor)
    ActionResult(newPiece, newCursor, selection, redraw = true)
  }

  override def name: String = "Insert Rest"
}
