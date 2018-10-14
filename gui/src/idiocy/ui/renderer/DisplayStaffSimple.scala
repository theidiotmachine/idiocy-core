package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.clipboard.ClipboardMeasure
import idiocy.ui.utils.ArrayUtils
import idiocy.ui.{Cursor, EventSelection, MeasureSelection, Selection}


class DisplayStaffSimple(val clef: Clef, val measures: Array[DisplayStaffSimpleMeasure]) extends DisplayStaff {
  private [this] val numInvisibleSpaces = 2

  override def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int,
                                  cursor: Cursor): (DisplayStaff, Cursor) = {
    val measure = measures(cursor.measure)
    val (newMeasure, newCursor) = measure.insertNoteAtCursor(lengthPips, noteIdModifier,
      cursor)
    if(newMeasure.lengthPips >= newMeasure.timeSig.measureLengthPips){
      val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
      (new DisplayStaffSimple(clef, newMeasures :+ new DisplayStaffSimpleMeasure(
        newMeasure.timeSig, newMeasure.bpm, newMeasure.key, Array())
      ), newCursor.startOfNextMeasure)
    } else {
      val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
      (new DisplayStaffSimple(clef, newMeasures), newCursor)
    }
  }

  def heightPixels(displayParams: PieceDisplayParams): Int = {
    displayParams.staffLineSeparationPixels * (4 + numInvisibleSpaces * 2) +
      displayParams.staffLineWidthPixels * (5 + numInvisibleSpaces * 2)
  }


  override def bar0OffsPixels(displayParams: PieceDisplayParams): Int = ???

  override def render(graphics: Graphics,
                       offset: Point,
                       canvas: Dimension,
                       thisFirstMeasure: Int,
                       numMeasuresPerLine: Int,
                       staffId: Int,
                       cursor: Cursor,
                       selection: Selection,
                       displayParams: PieceDisplayParams): Unit = {
    var yOffs = numInvisibleSpaces * displayParams.staffLineSeparationPixels + (numInvisibleSpaces - 1) * displayParams.staffLineWidthPixels
    ???
  }

  /*
  def insertNoteIntoMeasures(note: Note, pieceDisplayParams: PieceDisplayParams): Array[DisplayStaffSimpleMeasure] = {
    if(!measures.isEmpty && note.startPips < measures.head.startPips)
      throw new IllegalArgumentException("event is too early")
    val measureIdx = measures.indexWhere(p => p.startPips >= note.startPips && note.startPips < p.endPips)
    if (measureIdx >= 0) {
      val measure = measures(measureIdx)
      val displayKey = getDisplayKey(measure.key, pieceDisplayParams)
      val displayNote = measure.convertNote(note, clef, displayKey)
      ArrayUtils.replaceElem(measures, measureIdx, measure.insertNote(displayNote))
    } else {
      var inserted = false
      val pips = if (measures.isEmpty) startPips else measures.last.endPips
      val timeSig = if (measures.isEmpty) TimeSig(4, 4) else measures.last.timeSig
      val bpm = if (measures.isEmpty) 120 else measures.last.bpm
      val key = if (measures.isEmpty) Key.CMajor else measures.last.key
      val newMeasures = ArrayBuffer[DisplayStaffSimpleMeasure]()
      while (!inserted) {
        var newMeasure = new DisplayStaffSimpleMeasure(pips, timeSig, bpm, key, Array())
        if (note.startPips >= newMeasure.startPips && note.startPips < newMeasure.endPips) {
          val displayKey = getDisplayKey(key, pieceDisplayParams)
          val displayNote = newMeasure.convertNote(note, clef, displayKey)
          newMeasure = newMeasure.insertNote(displayNote)
          inserted = true
        }
        newMeasures += newMeasure
      }
      measures ++ newMeasures
    }
  }
  */

  override def numMeasures: Int = measures.length

  override def getMeasure(measureIdx: Int): DisplayMeasure = measures(measureIdx)

  override def cursorMoveStaff(cursor: Cursor, direction: Int): Option[Cursor] = ???

  override def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (DisplayStaff, Cursor) = ???

  override def backspaceAtCursor(cursor: Cursor): (DisplayStaff, Cursor) = ???

  override def getTopCursor(staffId: Int, measure: Int, event: Int): Cursor = ???
  override def getBottomCursor(staffId: Int, measure: Int, event: Int): Cursor = ???

  /**
    * get a cursor position at the left of this measure
    *
    * @param staffId staffid
    * @param measure the measure we are at
    * @param barLine the barline we are at
    * @return
    */
  override def getLeftCursor(staffId: Int, measure: Int, barLine: Int): Cursor = ???

  override def getRightCursor(staffId: Int, measure: Int, barLine: Int): Cursor = ???

  override def getTopLeftCursor(staffId: Int, measure: Int): Cursor = ???

  override def getBottomRightCursor(staffId: Int, measure: Int): Cursor = ???

  override def getConstrainedCursor(staffId: Int, measureId: Int, eventId: Int, barLine: Int): Cursor = ???


  override def getClipboardMeasuresFromSelection(selection: EventSelection): Array[ClipboardMeasure] = ???
  override def getClipboardMeasuresFromSelection(selection: MeasureSelection): Array[ClipboardMeasure] = ???


  override def insertMeasures(measureId: Int, numMeasures: Int): DisplayStaff = ???
  override def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayStaff, Cursor) = ???

  override def deleteMeasures(measureId: Int, numMeasures: Int): DisplayStaff = ???
  override def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayStaff, Cursor) = ???
}
