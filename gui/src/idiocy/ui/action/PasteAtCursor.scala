package idiocy.ui.action
import idiocy.ui.clipboard.InternalClipboard
import idiocy.ui.music.Piece
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.{Cursor, Selection}

class PasteAtCursor(internalClipboard: InternalClipboard) extends SimpleUndoRedoAction {
  override def applyInternal(piece: Piece,
                             pieceDisplayParams: PieceDisplayParams,
                             cursor: Cursor,
                             selection: Selection): ActionResult = {
    val (pieceOut, newCursor) = piece.pasteFromClipboardAtCursor(cursor, internalClipboard)
    ActionResult(pieceOut, newCursor, selection, redraw = true)
  }

  override def name: String = "Paste"
}
