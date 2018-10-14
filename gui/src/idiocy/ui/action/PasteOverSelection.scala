package idiocy.ui.action

import idiocy.ui.clipboard.InternalClipboard
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}
import idiocy.ui.{Cursor, Selection}

class PasteOverSelection(internalClipboard: InternalClipboard) extends SimpleUndoRedoAction {
  override def applyInternal(displayPiece: DisplayPiece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection
                            ): ActionResult = {
    val (displayPieceOut, newCursor, newSelection) = displayPiece.pasteFromClipboardOverSelection(cursor, selection, internalClipboard)
    ActionResult(displayPieceOut, newCursor, newSelection, redraw = true)
  }

  override def name: String = "Paste"
}
