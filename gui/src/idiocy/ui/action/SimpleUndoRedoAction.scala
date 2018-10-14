package idiocy.ui.action

import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}



abstract class SimpleUndoRedoAction extends UserAction {
  var undoData: Option[ActionResult] = None
  var redoData: Option[ActionResult] = None

  override def undo(displayPiece: DisplayPiece,
                    pieceDisplayParams: PieceDisplayParams,
                    cursor: Cursor,
                    selection: Selection
                   ): ActionResult =
    undoData.get

  def applyInternal(displayPiece: DisplayPiece,
                    pieceDisplayParams: PieceDisplayParams,
                    cursor: Cursor,
                    selection: Selection
                   ): ActionResult

  override def apply(displayPiece: DisplayPiece,
                     pieceDisplayParams: PieceDisplayParams,
                     cursor: Cursor,
                     selection: Selection
                    ): ActionResult = {
    if(redoData.isEmpty){
      redoData = Some(applyInternal(displayPiece, pieceDisplayParams, cursor, selection))
      undoData = Some(ActionResult(displayPiece, cursor, selection, redoData.get.redraw))
    }
    redoData.get
  }
}
