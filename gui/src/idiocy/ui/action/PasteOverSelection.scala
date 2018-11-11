package idiocy.ui.action

import idiocy.ui.clipboard.InternalClipboard
import idiocy.ui.music.Piece
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.{Cursor, Selection}

class PasteOverSelection(internalClipboard: InternalClipboard) extends SimpleUndoRedoAction {
  override def applyInternal(piece: Piece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection
                            ): ActionResult = {
    val (pieceOut, newCursor, newSelection) = piece.pasteFromClipboardOverSelection(cursor, selection, internalClipboard)
    ActionResult(pieceOut, newCursor, newSelection, redraw = true)
  }

  override def name: String = "Paste"
}
