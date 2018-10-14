package idiocy.ui.action

import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}

class InsertRestAtCursor(val lengthPips: Int) extends SimpleUndoRedoAction {
  override def applyInternal(displayPiece: DisplayPiece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection
                            ): ActionResult = {
    val (displayPieceOut, newCursor) = displayPiece.insertRestAtCursor(lengthPips, cursor)
    ActionResult(displayPieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Insert Rest"
}
