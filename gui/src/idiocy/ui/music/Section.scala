package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.{Cursor, Selection}

trait Section {
  def selectionMove(sectionIds: Array[Int],
                    cursor: Cursor,
                    selection: Selection,
                    direction: Int,
                    numMeasuresPerLine: Int,
                    displayParams: PieceDisplayParams): Option[(Cursor, Selection)]

  def appendNewGrandStaff: Section

  def getLeftCursor(path: Array[Int], cursor: Cursor): Option[Cursor]
  def getRightCursor(path: Array[Int], cursor: Cursor): Option[Cursor]

  def cursorMove(path: Array[Int], cursor: Cursor, direction: Int, numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Option[Cursor]

  def replace(sectionIds: Array[Int], newLeafSection: LeafSection): Section

  val name: String

  def render(graphics: Graphics,
             offset: Point,
             canvas: Dimension,
             numMeasuresPerLine: Int,
             cursor: Cursor,
             selection: Selection,
             displayParams: PieceDisplayParams): Unit

  def pixelHeight(numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Int

  def isEmpty: Boolean
}
