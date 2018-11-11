package idiocy.ui.action

import idiocy.ui.music.Piece
import idiocy.ui.{Cursor, Selection}
import idiocy.ui.renderer.PieceDisplayParams

final case class ActionResult(
                               //displayPiece: DisplayPiece,
                               piece: Piece,
                               cursor: Cursor,

                               selection: Selection, redraw: Boolean)

trait UserAction {
  def apply(piece: Piece,
            pieceDisplayParams: PieceDisplayParams,
            cursor: Cursor,
            selection: Selection): ActionResult
  def undo(piece: Piece,
           pieceDisplayParams: PieceDisplayParams,
           cursor: Cursor,
           selection: Selection): ActionResult
  def name: String
}
