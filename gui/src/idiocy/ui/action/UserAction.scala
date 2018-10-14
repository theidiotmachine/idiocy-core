package idiocy.ui.action

import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.{DisplayPiece, PieceDisplayParams}

final case class ActionResult(displayPiece: DisplayPiece, cursor: Cursor, selection: Selection, redraw: Boolean)

trait UserAction {
  def apply(displayPiece: DisplayPiece, pieceDisplayParams: PieceDisplayParams, cursor: Cursor, selection: Selection):
  ActionResult
  def undo(displayPiece: DisplayPiece, pieceDisplayParams: PieceDisplayParams, cursor: Cursor, selection: Selection):
  ActionResult
  def name: String
}
