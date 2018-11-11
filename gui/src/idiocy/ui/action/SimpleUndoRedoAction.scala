package idiocy.ui.action

import idiocy.ui.music.Piece
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.PieceDisplayParams



abstract class SimpleUndoRedoAction extends UserAction {
  var undoData: Option[ActionResult] = None
  var redoData: Option[ActionResult] = None

  override def undo(piece: Piece,
                    pieceDisplayParams: PieceDisplayParams,
                    cursor: Cursor,
                    selection: Selection
                   ): ActionResult =
    undoData.get

  def applyInternal(piece: Piece,
                    pieceDisplayParams: PieceDisplayParams,
                    cursor: Cursor,
                    selection: Selection
                   ): ActionResult

  override def apply(piece: Piece,
                     pieceDisplayParams: PieceDisplayParams,
                     cursor: Cursor,
                     selection: Selection
                    ): ActionResult = {
    if(redoData.isEmpty){
      redoData = Some(applyInternal(piece, pieceDisplayParams, cursor, selection))
      undoData = Some(ActionResult(piece, cursor, selection, redoData.get.redraw))
    }
    redoData.get
  }
}
