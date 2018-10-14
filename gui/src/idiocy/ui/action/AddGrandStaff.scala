package idiocy.ui.action
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}

class AddGrandStaff extends SimpleUndoRedoAction {
  override def applyInternal(displayPiece: DisplayPiece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection): ActionResult = {
    pieceDisplayParams.trackVisibility += true
    val displayPieceOut = displayPiece.appendNewGrandStaff(displayPiece.system.staffs.length)
    ActionResult(displayPieceOut, cursor, selection, redraw = true)
  }

  override def name: String = "Add Grand Staff"
}
