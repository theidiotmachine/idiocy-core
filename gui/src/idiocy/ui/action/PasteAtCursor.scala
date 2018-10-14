package idiocy.ui.action
import idiocy.ui.clipboard.InternalClipboard
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}

class PasteAtCursor(internalClipboard: InternalClipboard) extends SimpleUndoRedoAction {
  override def applyInternal(displayPiece: DisplayPiece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection): ActionResult = {
    val (displayPieceOut, newCursor) = displayPiece.pasteFromClipboardAtCursor(cursor, internalClipboard)
    ActionResult(displayPieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Paste"
}
