package idiocy.ui.action
import idiocy.ui.music.Piece
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.{Cursor, Selection}

class InsertMeasureAtCursor extends SimpleUndoRedoAction {
  override def applyInternal(piece: Piece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection): ActionResult = {
    val (pieceOut, newCursor) = piece.insertMeasuresAtCursor(cursor, 1)
    ActionResult(pieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Insert Measure"
}
