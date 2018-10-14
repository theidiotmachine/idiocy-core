package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.music.key.Key
import idiocy.ui._
import idiocy.ui.clipboard.ClipboardMeasure
import idiocy.ui.utils.ArrayUtils
object DisplayStaff{
  def smoosh(events: Array[DisplayEventSet]): Array[DisplayEventSet] = {
    var out = events
    var i = 0
    while(i < out.length - 1){
      var smooshCandidate = true
      val theseEvents = events(i)
      val nextEvents = events(i+1)
      val theseEventsArray = theseEvents.events
      var nextEventsArray = nextEvents.events
      var j = 0
      while(j < theseEvents.length && smooshCandidate){
        val event = theseEventsArray(j)
        val oei: Option[Int] = event match{
          case displayNote: DisplayNote =>
            ArrayUtils.findIndex[DisplayEvent](nextEventsArray, {
              case nextDisplaySpan: DisplaySpan => nextDisplaySpan.barLocation == displayNote.barLocation
              case _ => false
            })

          case displaySpan: DisplaySpan =>
            ArrayUtils.findIndex[DisplayEvent](nextEventsArray, {
              case nextDisplaySpan: DisplaySpan => nextDisplaySpan.barLocation == displaySpan.barLocation
              case _ => false
            })

          case displayRest: DisplayPartialRest =>
            ArrayUtils.findIndex[DisplayEvent](nextEventsArray, {
              case nextDisplayRest: DisplayPartialRest => displayRest.barLocation == nextDisplayRest.barLocation
              case _ => false
            })

          case displayFullRest: DisplayFullRest =>
            ArrayUtils.findIndex[DisplayEvent](nextEventsArray, {
              case nextDisplayFullRest: DisplayFullRest => displayFullRest.barLocation == nextDisplayFullRest.barLocation
              case _ => false
            })

          case _ => None
        }

        if(oei.isEmpty)
          smooshCandidate = false
        else
          nextEventsArray = ArrayUtils.removeIndex(nextEventsArray, oei.get)

        j += 1
      }

      if(!nextEventsArray.isEmpty){
        smooshCandidate = false
      }

      if(!smooshCandidate){
        i += 1
      }
      else{
        //smoosh
        val smooshedEventSet = DisplayEventSet.extendEventSet(theseEvents, theseEvents.lengthPips + nextEvents.lengthPips)
        out = ArrayUtils.replaceElem(out, i, smooshedEventSet)
        out = ArrayUtils.removeIndex(out, i+1)
      }
    }
    out
  }
}

trait DisplayStaff {
  def getClipboardMeasuresFromSelection(selection: EventSelection): Array[ClipboardMeasure]
  def getClipboardMeasuresFromSelection(selection: MeasureSelection): Array[ClipboardMeasure]

  /**
    * get a cursor positon at the top of this staff
    *
    * @param staffId staffid
    * @param measureId the measure we are at
    * @param event the event we are at
    * @return
    */
  def getTopCursor(staffId: Int, measureId: Int, event: Int): Cursor

  /**
    * get a cursor position at the bottom of this staff
    * @param staffId staffid
    * @param measureId the measure we are at
    * @param event the event we are at
    * @return
    */
  def getBottomCursor(staffId: Int, measureId: Int, event: Int): Cursor

  /**
    * get a cursor position at the left of this measure
    * @param staffId staffid
    * @param measureId the measure we are at
    * @param barLine the barline we are at
    * @return
    */
  def getLeftCursor(staffId: Int, measureId: Int, barLine: Int): Cursor
  def getRightCursor(staffId: Int, measureId: Int, barLine: Int): Cursor
  def getTopLeftCursor(staffId: Int, measureId: Int): Cursor
  def getBottomRightCursor(staffId: Int, measureId: Int): Cursor
  def getConstrainedCursor(staffId: Int, measureId: Int, eventId: Int, barLine: Int): Cursor

  def backspaceAtCursor(cursor: Cursor): (DisplayStaff, Cursor)
  def insertMeasures(measureId: Int, numMeasures: Int): DisplayStaff
  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayStaff, Cursor)
  def deleteMeasures(measureId: Int, numMeasures: Int): DisplayStaff
  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplayStaff, Cursor)

  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (DisplayStaff, Cursor)

  def getMeasure(measureIdx: Int): DisplayMeasure

  def cursorMoveStaff(cursor: Cursor, direction: Int): Option[Cursor]

  def cursorMove(cursor: Cursor, direction: Int): Option[Cursor] = {
    direction match {
      case Cursor.LEFT =>
        if(cursor.event == 0) {
          if(cursor.measure > 0) {
            cursorMoveEndOfPrevMeasure(cursor)
          } else
            Some(cursor)
        } else {
          Some(cursor.prevEvent)
        }

      case Cursor.CONTROL_LEFT =>
        if(cursor.measure > 0)
          cursorMoveSameEventOfPrevMeasure(cursor)
        else
          Some(Cursor(cursor.staff, 0, 0, cursor.barLine))

      case Cursor.RIGHT =>
        val measure = getMeasure(cursor.measure)
        if(cursor.event == measure.events.length){
          val nextMeasureIdx = cursor.measure + 1
          if(nextMeasureIdx >= numMeasures){
            Some(cursor)
          } else
            Some(cursor.startOfNextMeasure)
        } else {
          Some(cursor.nextEvent)
        }

      case Cursor.CONTROL_RIGHT =>
        val measure = getMeasure(cursor.measure)
        if(cursor.measure == numMeasures - 1)
          Some(Cursor(cursor.staff, cursor.measure, measure.events.length, cursor.barLine))
        else
          cursorMoveSameEventOfNextMeasure(cursor)

      case Cursor.UP =>
        cursorMoveStaff(cursor, direction)

      case Cursor.DOWN =>
        cursorMoveStaff(cursor, direction)

      case Cursor.CONTROL_DOWN | Cursor.CONTROL_UP =>
        None
    }
  }

  private [this] def cursorMoveEndOfPrevMeasure(cursor: Cursor): Option[Cursor] = {
    val prevMeasureId = cursor.measure - 1
    val prevMeasure = getMeasure(prevMeasureId)
    val newEventId = prevMeasure.events.length
    Some(new Cursor(cursor.staff, prevMeasureId, newEventId, cursor.barLine))
  }

  private [this] def cursorMoveSameEventOfPrevMeasure(cursor: Cursor): Option[Cursor] = {
    val prevMeasureId = cursor.measure - 1
    val prevMeasure = getMeasure(prevMeasureId)
    val newEventId = math.min(cursor.event, prevMeasure.events.length)
    Some(new Cursor(cursor.staff, prevMeasureId, newEventId, cursor.barLine))
  }

  private [this] def cursorMoveSameEventOfNextMeasure(cursor: Cursor): Option[Cursor] = {
    val nextMeasureId = cursor.measure + 1
    val nextMeasure = getMeasure(nextMeasureId)
    val newEventId = math.min(cursor.event, nextMeasure.events.length)
    Some(new Cursor(cursor.staff, nextMeasureId, newEventId, cursor.barLine))
  }

  def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int,
                         cursor: Cursor): (DisplayStaff, Cursor)

  def numMeasures: Int

  def render(graphics: Graphics,
             offset: Point,
             canvas: Dimension,
             thisFirstMeasure: Int,
             numMeasuresPerLine: Int,
             staffId: Int,
             cursor: Cursor,
             selection: Selection,
             displayParams: PieceDisplayParams): Unit

  def renderBarLines(graphics: Graphics, offset: Point, canvas: Dimension, howMany: Int, displayParams: PieceDisplayParams): Int = {
    var i = 0
    var yOffs = 0
    graphics.setColor(GlobalUISettings.palette.staffColor)
    while(i < howMany - 1){
      graphics.drawLine(offset.x, offset.y + yOffs, offset.x + canvas.width, offset.y + yOffs)
      yOffs += displayParams.staffLineSeparationPixels + displayParams.staffLineWidthPixels
      i += 1
    }
    graphics.drawLine(offset.x, offset.y + yOffs, offset.x + canvas.width, offset.y + yOffs)
    yOffs += displayParams.staffLineWidthPixels
    yOffs
  }

  val clef: Clef

  def getDisplayKey(key: Key, pieceDisplayParams: PieceDisplayParams): Key = {
    if(pieceDisplayParams.showAsTraditionalKeySig) key.asTraditionalKey else key
  }

  def heightPixels(displayParams: PieceDisplayParams): Int
  def bar0OffsPixels(displayParams: PieceDisplayParams): Int
}
