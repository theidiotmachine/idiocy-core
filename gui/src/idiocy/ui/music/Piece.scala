package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.clipboard.{ClipboardContentsEmpty, InternalClipboard}
import idiocy.ui._
import idiocy.ui.renderer.PieceDisplayParams

class Piece(val section: Section) {
  def renderCursor(graphics: Graphics, cursor: Cursor, numMeasuresPerLine: Int): Unit = {

  }

  def cursorMove(cursor: Cursor, direction: Int, numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Cursor = {
    section.cursorMove(cursor.sectionIds, cursor, direction, numMeasuresPerLine, displayParams).getOrElse(cursor)
  }

  def selectionMove(cursor: Cursor,
                    selection: Selection,
                    direction: Int,
                    numMeasuresPerLine: Int,
                    displayParams: PieceDisplayParams): (Cursor, Selection) = {
    section.selectionMove(cursor.sectionIds, cursor, selection, direction,
      numMeasuresPerLine, displayParams).getOrElse((cursor, selection))
  }

  def pasteFromClipboardOverSelection(cursor: Cursor, selection: Selection, internalClipboard: InternalClipboard): (Piece, Cursor, Selection) = ???

  def pasteFromClipboardAtCursor(cursor: Cursor, internalClipboard: InternalClipboard): (Piece, Cursor) = ???

  def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int, cursor: Cursor): (Piece, Cursor) =
    leafSectionOp(leafSection=>leafSection.insertNoteAtCursor(lengthPips, noteIdModifier, cursor), cursor)

  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (Piece, Cursor) =
    leafSectionOp(leafSection=>leafSection.insertRestAtCursor(lengthPips, cursor), cursor)

  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Piece, Cursor) =
    leafSectionOp(leafSection=>leafSection.insertMeasuresAtCursor(cursor, numMeasures), cursor)

  private [this] def leafSectionOp(f: LeafSection => (LeafSection, Cursor), cursor: Cursor): (Piece, Cursor) = {
    val leafSection = getLeafSection(cursor.sectionIds)
    val (newLeafSection, newCursor) = f(leafSection)
    val newSection = section.replace(cursor.sectionIds, newLeafSection)
    (new Piece(newSection), newCursor)
  }

  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Piece, Cursor) =
    leafSectionOp(leafSection=>leafSection.deleteMeasuresAtCursor(cursor, numMeasures), cursor)

  def backspaceAtCursor(cursor: Cursor): (Piece, Cursor) =
    leafSectionOp(leafSection=>leafSection.backspaceAtCursor(cursor), cursor)

  def appendNewGrandStaff(cursor: Cursor, displayParams: PieceDisplayParams): (Piece, Cursor) = {
    val newSection = section.appendNewGrandStaff
    val newCursor = Cursor(cursor.sectionIds, displayParams.trackVisibility.length, 0, 0)
    displayParams.addNewTrack()
    (new Piece(newSection), newCursor)
  }

  def render(graphics: Graphics,
             offset: Point,
             canvas: Dimension,
             numMeasuresPerLine: Int,
             cursor: Cursor,
             selection: Selection,
             pieceDisplayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.bgColor)
    graphics.fillRect(0, 0, canvas.width, canvas.height)

    section.render(
      graphics,
      offset,
      canvas,
      numMeasuresPerLine,
      cursor,
      selection,
      pieceDisplayParams
    )
  }

  def getSection(sectionIds: Array[Int]): Section = {
    var out = section
    var i = 0
    while(i < sectionIds.length){
      out match {
        case branchSection: BranchSection =>
          out = branchSection.sections(sectionIds(i))
        case _: LeafSection =>
      }
      i += 1
    }
    out
  }

  def getFirstLeafSection(branchSection: BranchSection): LeafSection = {
    var section: Section = branchSection
    var out: Option[LeafSection] = None
    while(out.isEmpty){
      section match {
        case branchSection: BranchSection =>
          section = branchSection.sections(0)
        case leafSection: LeafSection =>
          out = Some(leafSection)
      }
    }
    out.get
  }


  def getLeafSection(sectionIds: Array[Int]): LeafSection = {
    val section = getSection(sectionIds)
    section match {
      case leafSection: LeafSection => leafSection
      case branchSection: BranchSection => getFirstLeafSection(branchSection)
    }
  }

  def isEmpty: Boolean = section.isEmpty

  def copyToClipboard(selection: Selection): InternalClipboard = {
    selection match {
      case eventSelection: EventSelection =>
        val leafSection = getLeafSection(eventSelection.sectionIds)
        leafSection.copyToClipboard(selection)
      case _: NoSelection => InternalClipboard(ClipboardContentsEmpty())
      case _ => ???
    }
  }
}
