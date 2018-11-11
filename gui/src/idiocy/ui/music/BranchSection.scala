package idiocy.ui.music
import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.{Cursor, GlobalUISettings, Selection}
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.utils.ArrayUtils

object BranchSection{
  val HeaderSpace = 18
}

class BranchSection(val name: String, val sections: Array[Section]) extends Section {

  override def replace(sectionIds: Array[Int], newLeafSection: LeafSection): Section = {
    val thisSectionId = sectionIds(0)
    val remainingSectionIds = sectionIds.drop(1)

    val newSubSection = sections(thisSectionId).replace(remainingSectionIds, newLeafSection)
    new BranchSection(name, ArrayUtils.replaceElem(sections, thisSectionId, newSubSection))
  }

  private [this] def renderArray(sections: Array[Section],
                                 graphics: Graphics,
                                 offset: Point,
                                 canvas: Dimension,
                                 numMeasuresPerLine: Int,
                                 cursor: Cursor,
                                 selection: Selection,
                                 displayParams: PieceDisplayParams
                                ): Unit = {
    var i = 0
    var yOffs = offset.y
    while(i < sections.length){
      val section = sections(i)
      val sectionPixelHeight = section.pixelHeight(numMeasuresPerLine, displayParams)
      section.render(graphics,
        new Point(offset.x, yOffs),
        new Dimension(canvas.width, sectionPixelHeight),
        numMeasuresPerLine,
        cursor,
        selection,
        displayParams
      )

      yOffs += sectionPixelHeight
      i += 1
    }
  }

  override def render(graphics: Graphics,
                      offset: Point,
                      canvas: Dimension,
                      numMeasuresPerLine: Int,
                      cursor: Cursor,
                      selection: Selection,
                      displayParams: PieceDisplayParams): Unit = {
    graphics.setColor(GlobalUISettings.palette.noteColor)
    graphics.setFont(GlobalUISettings.sectionNameFont)
    graphics.drawString(name, offset.x, offset.y + 12)

    renderArray(sections,
      graphics,
      new Point(offset.x, offset.y + BranchSection.HeaderSpace),
      new Dimension(canvas.width, canvas.height - BranchSection.HeaderSpace),
      numMeasuresPerLine,
      cursor,
      selection,
      displayParams
    )
  }

  override def pixelHeight(numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Int = {
    sections.foldLeft(0)((b, s)=>b + s.pixelHeight(numMeasuresPerLine, displayParams))
  }

  override def isEmpty: Boolean = sections.isEmpty

  override def cursorMove(path: Array[Int], cursor: Cursor, direction: Int, numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Option[Cursor] = {
    val idx = path.head
    val remainingPath = path.drop(1)
    val section = sections(idx)
    val oCursor = section.cursorMove(remainingPath, cursor, direction, numMeasuresPerLine, displayParams)
    if(oCursor.isDefined)
      oCursor
    else{
      direction match{
        case Cursor.LEFT =>
          if(idx > 0){
            val prevSection = sections(idx - 1)
            prevSection.getRightCursor(remainingPath, cursor)
          } else
            None

        case Cursor.RIGHT =>
          if(idx+1 < sections.length){
            val nextSection = sections(idx + 1)
            nextSection.getLeftCursor(remainingPath, cursor)
          } else
            None

        case _ => ???
      }
    }
  }


  override def selectionMove(sectionIds: Array[Int],
                             cursor: Cursor,
                             selection: Selection,
                             direction: Int,
                             numMeasuresPerLine: Int,
                             displayParams: PieceDisplayParams): Option[(Cursor, Selection)] = {
    val idx = sectionIds.head
    val remainingPath = sectionIds.drop(1)
    val section = sections(idx)
    //right now, won't hold anything more than one section
    section.selectionMove(remainingPath, cursor, selection, direction, numMeasuresPerLine, displayParams)
  }

  override def getLeftCursor(path: Array[Int], cursor: Cursor): Option[Cursor] = {
    sections.last.getLeftCursor(path.drop(1), cursor)
  }

  override def getRightCursor(path: Array[Int], cursor: Cursor): Option[Cursor] = {
    sections.last.getRightCursor(path.drop(1), cursor)
  }

  override def appendNewGrandStaff: Section = {
    val newSections = sections.map(x=>x.appendNewGrandStaff)
    new BranchSection(name, newSections)
  }
}
