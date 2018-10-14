package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui._
import idiocy.ui.clipboard.ClipboardMeasure
import idiocy.ui.utils.ArrayUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
/*

 13 b5
 12 a5 .
 11 g5
 10 f5 --
  9 e5
  8 d5 --
  7 c5
  6 b4 --
  5 a4
  4 g4 --
  3 f4
  2 e4 --
  1 d4
  0 c4 .
 -1 b3
 -2 a3 --
 -3 g3
 -4 f3 --
 -5 e3
 -6 d3 --
 -7 c3
 -8 b2 --
 -9 a2
-10 g2 --
-11 f2
-12 e2 .
-13 d2

 */

object DisplayGrandStaff{
  private [this] def createMap(): Map[String,  Int] = {
    var pClassIdx = 1
    var octaveNumber = 2
    var barLine = MIN_BAR_LINE
    val out = mutable.Map[String, Int]()
    val pClasses = Array("C", "D",  "E", "F", "G", "A", "B")
    while(barLine <= MAX_BAR_LINE){
      out += ((pClasses(pClassIdx)+octaveNumber, barLine))
      pClassIdx += 1
      if(pClassIdx == pClasses.length){
        pClassIdx = 0
        octaveNumber += 1
      }
      barLine += 1
    }
    out.toMap
  }
  private [this] val pitchClassNameAndOctaveNumberToBarLineMap: Map[String,  Int] = createMap()
  def pitchClassNameAndOctaveNumberToBarLine(pitchClass: String, octave: Int): Int = {
    pitchClassNameAndOctaveNumberToBarLineMap(pitchClass+octave)
  }

  val MAX_BAR_LINE = 13
  val MIN_BAR_LINE: Int = -13
}

final class DisplayGrandStaff(val measures: Array[DisplayGrandStaffMeasure]) extends DisplayStaff{
  val clef: Clef = GrandClef
  private [this] val numInvisibleSpaces = 2

  override def render(graphics: Graphics,
                      offset: Point,
                      canvas: Dimension,
                      firstMeasure: Int,
                      numMeasuresPerLine: Int,
                      staffId: Int,
                      cursor: Cursor,
                      selection: Selection,
                      displayParams: PieceDisplayParams): Unit = {
    val bar0Offs = bar0OffsPixels(displayParams)
    val measureWidth = (canvas.width - displayParams.clefWidthPixels) / numMeasuresPerLine
    var measureId = firstMeasure
    var xOffs = displayParams.clefWidthPixels
    var displayKey = true
    while(measureId < firstMeasure + numMeasuresPerLine && measureId < measures.length){
      val measure = measures(measureId)

      measure.render(graphics, staffId, measureId, selection, new Point(offset.x + xOffs, offset.y), bar0Offs,
        new Dimension(measureWidth, canvas.height), displayKey, displayParams)

      cursor match {
        case cursor: Cursor =>
          if(cursor.staff == staffId &&
            cursor.measure == measureId){
            measure.renderInsertCursor(graphics, cursor, new Point(offset.x + xOffs, offset.y), bar0Offs,
              new Dimension(measureWidth, canvas.height), displayParams)
          }
      }

      xOffs += measureWidth
      displayKey = false
      measureId += 1
    }
  }

  override def heightPixels(displayParams: PieceDisplayParams): Int = {
    displayParams.staffLineSeparationPixels * (10 + numInvisibleSpaces * 2) +
      displayParams.staffLineWidthPixels * (9 + numInvisibleSpaces * 2)
  }

  override def bar0OffsPixels(displayParams: PieceDisplayParams): Int = {
    displayParams.staffLineSeparationPixels * (5 + numInvisibleSpaces) +
      displayParams.staffLineWidthPixels * (4 + numInvisibleSpaces)
  }

  override def insertNoteAtCursor(lengthPips: Int,
                                  noteIdModifier: Int,
                                  cursor: Cursor): (DisplayStaff, Cursor) = {
    val (newMeasure, newCursor) = measures(cursor.measure).insertNoteAtCursor(
      lengthPips, noteIdModifier, cursor)
    if(newMeasure.lengthPips >= newMeasure.timeSig.measureLengthPips){
      val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
      (new DisplayGrandStaff(newMeasures :+ newMeasure.cloneEmpty), newCursor.startOfNextMeasure)
    } else {
      val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
      (new DisplayGrandStaff(newMeasures), newCursor)
    }
  }

  override def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (DisplayStaff, Cursor) = {
    val (newMeasure, newCursor) = measures(cursor.measure).insertRestAtCursor(
      lengthPips, cursor)
    if(newMeasure.lengthPips >= newMeasure.timeSig.measureLengthPips){
      val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
      (new DisplayGrandStaff(newMeasures :+ new DisplayGrandStaffMeasure(
        newMeasure.timeSig, newMeasure.bpm, newMeasure.key, Array())
      ), newCursor.startOfNextMeasure)
    } else {
      val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
      (new DisplayGrandStaff(newMeasures), newCursor)
    }
  }


  override def backspaceAtCursor(cursor: Cursor): (DisplayStaff, Cursor) = {
    val (newMeasure, newCursor) = measures(cursor.measure).backspace(cursor)
    val newMeasures = ArrayUtils.replaceElem(measures, cursor.measure, newMeasure)
    (new DisplayGrandStaff(newMeasures), newCursor)
  }

  override def numMeasures: Int = measures.length

  override def getMeasure(measureIdx: Int): DisplayMeasure = measures(measureIdx)

  override def cursorMoveStaff(cursor: Cursor, direction: Int): Option[Cursor] = {
    direction match {
      case Cursor.UP =>
        if(cursor.barLine < DisplayGrandStaff.MAX_BAR_LINE) Some(cursor.upABarLine) else None
      case Cursor.DOWN =>
        if(cursor.barLine > DisplayGrandStaff.MIN_BAR_LINE) Some(cursor.downABarLine) else None
    }
  }

  override def getTopCursor(staffId: Int, measureId: Int, eventId: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    if(m < measureId){
      new Cursor(staffId, m, 0, DisplayGrandStaff.MAX_BAR_LINE)
    } else {
      val e = math.min(eventId, measures(m).events.length)
      new Cursor(staffId, m, e, DisplayGrandStaff.MAX_BAR_LINE)
    }
  }

  override def getBottomCursor(staffId: Int, measureId: Int, eventId: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    if(m < measureId){
      new Cursor(staffId, m, 0, DisplayGrandStaff.MIN_BAR_LINE)
    } else {
      val e = math.min(eventId, measures(m).events.length)
      new Cursor(staffId, m, e, DisplayGrandStaff.MIN_BAR_LINE)
    }
  }

  override def getLeftCursor(staffId: Int, measureId: Int, barLine: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    new Cursor(staffId, m, 0, barLine)
  }

  override def getRightCursor(staffId: Int, measureId: Int, barLine: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    new Cursor(staffId, m, measures(m).events.length, barLine)
  }

  override def getTopLeftCursor(staffId: Int, measureId: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    new Cursor(staffId, m, 0, DisplayGrandStaff.MIN_BAR_LINE)
  }

  override def getBottomRightCursor(staffId: Int, measureId: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    new Cursor(staffId, m, measures(m).events.length, DisplayGrandStaff.MAX_BAR_LINE)
  }

  override def getConstrainedCursor(staffId: Int, measureId: Int, eventId: Int, barLine: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    val e = math.min(measures(m).events.length, eventId)
    val b = math.min(DisplayGrandStaff.MAX_BAR_LINE, math.max(DisplayGrandStaff.MIN_BAR_LINE, barLine))
    new Cursor(staffId, m, e, b)
  }

  override def getClipboardMeasuresFromSelection(selection: EventSelection): Array[ClipboardMeasure] = {
    val out = ArrayBuffer[ClipboardMeasure]()
    var measureId = selection.measureL
    while(measureId <= selection.measureR && measureId < measures.length - 1){
      val measure = measures(measureId)
      val (firstEventId, cmbt) = if(measureId == selection.measureL)
        (selection.eventL, ClipboardMeasure.MEASURE_TYPE_STUB)
      else
        (0, ClipboardMeasure.MEASURE_TYPE_FULL)

      val (lastEventId, cmet) = if(measureId == selection.measureR)
        (selection.eventR, ClipboardMeasure.MEASURE_TYPE_STUB)
      else
        (measure.events.length, ClipboardMeasure.MEASURE_TYPE_FULL)

      val ces = measure.getClipboardEventSets(firstEventId, lastEventId)

      out += new ClipboardMeasure(ces, cmbt, cmet)

      measureId += 1
    }
    out.toArray
  }

  override def getClipboardMeasuresFromSelection(selection: MeasureSelection): Array[ClipboardMeasure] = {
    val out = ArrayBuffer[ClipboardMeasure]()
    var measureId = selection.measureL
    while(measureId <= selection.measureR && measureId < measures.length - 1){
      val measure = measures(measureId)
      val ces = measure.getClipboardEventSets(0, measure.events.length)
      out += new ClipboardMeasure(ces, ClipboardMeasure.MEASURE_TYPE_FULL, ClipboardMeasure.MEASURE_TYPE_FULL)
      measureId += 1
    }
    out.toArray
  }

  override def insertMeasures(measureId: Int, numMeasures: Int): DisplayStaff = {
    val measure = measures(measureId)
    val insertedMeasures: Array[DisplayGrandStaffMeasure] = cloneEmptyMeasures(measure, numMeasures)
    val newMeasures = ArrayUtils.insertElemsAndShift(measures, measureId, insertedMeasures)
    new DisplayGrandStaff(newMeasures)
  }

  private [this] def cloneEmptyMeasures(measure: DisplayGrandStaffMeasure, numMeasures: Int): Array[DisplayGrandStaffMeasure] = {
    val clonedMeasures = new Array[DisplayGrandStaffMeasure](numMeasures)
    var i = 0
    while (i < numMeasures) {
      clonedMeasures(i) = measure.cloneEmpty
      i += 1
    }
    clonedMeasures
  }

  override def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayStaff, Cursor) = {
    val measure = measures(cursor.measure)
    if(cursor.event == 0)
      (insertMeasures(cursor.measure, numMeasures), cursor)
    else if(cursor.event == measure.events.length)
      (insertMeasures(cursor.measure + 1, numMeasures), cursor)
    else{

      //split the measure
      var headEvents = measure.events.slice(0, cursor.event)
      var tailEvents = measure.events.slice(cursor.event, measure.events.length)

      //generate some empty measures
      val emptyMeasures = cloneEmptyMeasures(measure, numMeasures)

      //if we cut a note from its spans move them into the head
      if(tailEvents.length > 0){
        //so look a the first set in the tail, find the spans
        var thisTailSet = tailEvents(0)
        var newThisTailSet = ArrayBuffer[DisplayEvent]()
        var newLastHeadSet = ArrayBuffer[DisplayEvent]()
        var copySet = ArrayBuffer[Int]()
        var j = 0
        while(j < thisTailSet.length){
          val e = thisTailSet(j)
          e match{
            case span: DisplaySpan =>
              copySet += span.barLocation
              newLastHeadSet += e
            case _ =>
              newThisTailSet += e
          }
          j += 1
        }

        //cut them over
        if(copySet.nonEmpty){
          headEvents = headEvents :+ new DisplayEventSet(newLastHeadSet.toArray, thisTailSet.lengthPips)
          tailEvents = ArrayUtils.replaceElem(tailEvents, 0, new DisplayEventSet(newThisTailSet.toArray, thisTailSet.lengthPips))
        }

        //now keep looking through the tail, moving the spans
        var i = 1
        while(copySet.nonEmpty && i < tailEvents.length){

          val oldCopySet = copySet
          copySet = ArrayBuffer[Int]()

          thisTailSet = tailEvents(i)
          newThisTailSet = ArrayBuffer[DisplayEvent]()
          newLastHeadSet = ArrayBuffer[DisplayEvent]()
          j = 0
          while(j < oldCopySet.length){
            val eventIdxs = thisTailSet.findEventIndexes(oldCopySet(j))
            var k = 0
            while(k < eventIdxs.length){
              val e = thisTailSet(k)
              e match{
                case _: DisplaySpan =>
                  copySet += j
                  newLastHeadSet += e
                case _ =>
                  newThisTailSet += e
              }
              k += 1
            }
            j +=  1
          }

          if(newLastHeadSet.nonEmpty)
            headEvents = headEvents :+ new DisplayEventSet(newLastHeadSet.toArray, thisTailSet.lengthPips)
          tailEvents = ArrayUtils.replaceElem(tailEvents, i, new DisplayEventSet(newThisTailSet.toArray, thisTailSet.lengthPips))

          i += 1
        }
      }

      //these now replace the original
      headEvents = DisplayGrandStaffMeasure.fixUp(headEvents)
      tailEvents = DisplayGrandStaffMeasure.fixUp(tailEvents)
      val replaceMeasures = measure.cloneNewEvents(headEvents) +: emptyMeasures :+ measure.cloneNewEvents(tailEvents)


      val newMeasures = ArrayUtils.replaceElems(measures, cursor.measure, 1, replaceMeasures)
      val newStaff = new DisplayGrandStaff(newMeasures)
      (newStaff, cursor)
    }

  }

  override def deleteMeasures(measureId: Int, numMeasures: Int): DisplayStaff = {
    var newMeasures = ArrayUtils.removeIndexes(measures, measureId, numMeasures)
    if(newMeasures.isEmpty){
      val oldFirstMeasure = measures(0)
      newMeasures = Array(oldFirstMeasure.cloneEmpty)
    }
    new DisplayGrandStaff(newMeasures)
  }

  override def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayStaff, Cursor) = {
    val newStaff = deleteMeasures(cursor.measure, numMeasures)
    val dgs = newStaff.asInstanceOf[DisplayGrandStaff]
    val newMeasureId = if(dgs.measures.length >= cursor.measure) dgs.measures.length - 1 else cursor.measure
    val newEventId = if(dgs.measures(newMeasureId).events.length > cursor.event)
      dgs.measures(newMeasureId).events.length
    else
      cursor.event
    (newStaff, Cursor(cursor.staff, newMeasureId, newEventId, cursor.barLine))
  }
}
