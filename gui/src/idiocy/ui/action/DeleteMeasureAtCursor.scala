package idiocy.ui.action
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}

class DeleteMeasureAtCursor extends SimpleUndoRedoAction{
  override def applyInternal(displayPiece: DisplayPiece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection): ActionResult = {
    val (displayPieceOut, newCursor) = displayPiece.deleteMeasuresAtCursor(cursor, 1)
    ActionResult(displayPieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Delete Measure"
}