package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.music.key.Key
import idiocy.ui.clipboard.ClipboardEventSet
import idiocy.ui.data.TimeSig
import idiocy.ui.music.event.{MusicEventSet, TimeSigEvent}
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui._

import scala.collection.mutable.ArrayBuffer

object Staff{

}

trait Staff {
  val events: Array[MusicEventSet]

  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (Staff, Cursor)

  def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int, cursor: Cursor): (Staff, Cursor)

  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Staff, Cursor)

  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Staff, Cursor)

  def backspaceAtCursor(cursor: Cursor): (Staff, Cursor)

  def lineHeightPixels(displayParams: PieceDisplayParams): Int
  def render(graphics: Graphics,
             offset: Point,
             canvas: Dimension,
             thisFirstMeasure: Int,
             numMeasuresPerLine: Int,
             staffId: Int,
             cursor: Cursor,
             selection: Selection,
             displayParams: PieceDisplayParams): Unit
  def getClipboardEventsFromSelection(selection: EventSelection): Array[ClipboardEventSet]
  def getClipboardEventsFromSelection(selection: MeasureSelection): Array[ClipboardEventSet]

  def getTimeSig(eventId: Int): TimeSig = {
    var i = math.min(eventId, events.length - 1)
    var oTimeSig: Option[TimeSig] = None
    while(i >= 0 && oTimeSig.isEmpty){
      val e = events(i)
      oTimeSig = e.getTimeSig
      i -= 1
    }
    oTimeSig.getOrElse(TimeSig.timeSig44)
  }

  def getKey(eventId: Int): Key = {
    var i = math.min(eventId, events.length - 1)
    var oKey: Option[Key] = None
    while(i >= 0 && oKey.isEmpty){
      val e = events(i)
      oKey = e.getKey
      i -= 1
    }
    oKey.getOrElse(Key.CMajor)
  }

  def getMeasure(measureIdx: Int): Option[Measure]
  def getMeasureUnsafe(measureIdx: Int): Measure
  def numMeasures: Int

  def cursorMoveVerticalWithinStaff(cursor: Cursor, direction: Int): Option[Cursor]

  /*
  def cursorMoveLeftOneMeasureHorizontally(cursor: Cursor): Option[Cursor] = {
    var eventId = cursor.event

    val timeSig = getTimeSig(eventId)
    var measureLengthPips = timeSig.measureLengthPips
    while(measureLengthPips > 0 && eventId >= 0){
      measureLengthPips -= events(eventId).lengthPips
      eventId -= 1
    }

    if(measureLengthPips > 0)
      None
    else
      Some(Cursor(cursor.sectionIds, cursor.staff, eventId, cursor.barLine))
  }

  def cursorMoveRightOneMeasureHorizontally(cursor: Cursor): Option[Cursor] = {
    var eventId = cursor.event

    val timeSig = getTimeSig(eventId)
    var measureLengthPips = timeSig.measureLengthPips
    while(measureLengthPips > 0 && eventId < events.length){
      measureLengthPips -= events(eventId).lengthPips
      eventId += 1
    }

    if(measureLengthPips > 0)
      None
    else
      Some(Cursor(cursor.sectionIds, cursor.staff, eventId, cursor.barLine))
  }

  def cursorMoveMeasuresHorizontally(cursor: Cursor, numMeasures: Int): Option[Cursor] = {
    var c: Option[Cursor] = Some(cursor)
    var n = numMeasures
    if(n < 0){
      while(n < 0 && c.isDefined){
        c = cursorMoveLeftOneMeasureHorizontally(cursor)
        n += 1
      }
    } else{
      while(n >= 0 && c.isDefined){
        c = cursorMoveRightOneMeasureHorizontally(cursor)
        n -= 1
      }
    }
    c
  }

  def cursorMove(cursor: Cursor, direction: Int): Option[Cursor] = {
    direction match {
      case Cursor.LEFT =>
        if(cursor.event == 0) None else Some(cursor.prevEvent)

      case Cursor.CONTROL_LEFT =>
        cursorMoveMeasuresHorizontally(cursor, -1)

      case Cursor.RIGHT =>
        if(cursor.event > events.length)
          None
        else
          Some(cursor.nextEvent)

      case Cursor.CONTROL_RIGHT =>
        cursorMoveMeasuresHorizontally(cursor, 1)

      case Cursor.UP | Cursor.DOWN =>
        cursorMoveVertical(cursor, direction)

      case Cursor.CONTROL_DOWN | Cursor.CONTROL_UP =>
        ???
    }
  }
  */

  /*
  def getLeftCursor(path: Array[Int], staffId: Int, barLine: Int): Cursor = Cursor(path, staffId, 0, barLine)
  def getRightCursor(path: Array[Int], staffId: Int, barLine: Int): Cursor = Cursor(path, staffId, events.length, barLine)
*/

  def getTopCursor(sectionIds: Array[Int], staffId: Int, meid: MeasureEventId): Cursor
  def getBottomCursor(sectionIds: Array[Int], staffId: Int, meid: MeasureEventId): Cursor
  def getLeftCursor(sectionIds: Array[Int], staffId: Int, measureId: Int, barLine: Int): Cursor
  def getRightCursor(sectionIds: Array[Int], staffId: Int, measureId: Int, barLine: Int): Cursor
  def getTopLeftCursor(sectionIds: Array[Int], staffId: Int, measureId: Int): Cursor
  def getBottomRightCursor(sectionIds: Array[Int], staffId: Int, measureId: Int): Cursor
  def getConstrainedCursor(sectionIds: Array[Int], staffId: Int, meid: MeasureEventId, barLine: Int): Cursor

  def cursorMove(cursor: Cursor, direction: Int): Option[Cursor] = {
    direction match {
      case Cursor.LEFT =>
        if(cursor.measureEventId.eventId == 0) {
          if(cursor.measureEventId.measureId > 0)
            cursorMoveEndOfPrevMeasure(cursor)
          else
            Some(cursor)
        } else
          Some(Cursor(cursor.sectionIds, cursor.staff, cursor.measureEventId.prevEvent, cursor.barLine))

      case Cursor.CONTROL_LEFT =>
        if(cursor.measureEventId.measureId > 0)
          cursorMoveSameEventOfPrevMeasure(cursor)
        else
          Some(Cursor(cursor.sectionIds, cursor.staff, MeasureEventId(0, 0), cursor.barLine))

      case Cursor.RIGHT =>
        val oMeasure = getMeasure(cursor.measureId)
        if(oMeasure.isDefined) {
          val measure = oMeasure.get
          if (cursor.measureEventId.eventId == measure.events.length) {
            val nextMeasureId = cursor.measureId + 1
            if (nextMeasureId >= numMeasures) {
              Some(cursor)
            } else
              Some(cursor.startOfNextMeasure)
          } else {
            Some(Cursor(cursor.sectionIds, cursor.staff, cursor.measureEventId.nextEvent, cursor.barLine))
          }
        } else {
          if(numMeasures == 0)
            Some(Cursor(cursor.sectionIds, cursor.staff, 0, 0, cursor.barLine))
          else
            Some(Cursor(cursor.sectionIds, cursor.staff, numMeasures - 1, 0, cursor.barLine))
        }

      case Cursor.CONTROL_RIGHT =>
        val oMeasure = getMeasure(cursor.measureId)
        if(oMeasure.isDefined) {
          val measure = oMeasure.get
          if (cursor.measureId == numMeasures - 1)
            Some(Cursor(cursor.sectionIds, cursor.staff,
              MeasureEventId(cursor.measureEventId.measureId, measure.events.length), cursor.barLine))
          else
            cursorMoveSameEventOfNextMeasure(cursor)
        } else
          Some(Cursor(cursor.sectionIds, cursor.staff, 0, 0, cursor.barLine))

      case Cursor.UP =>
        cursorMoveVerticalWithinStaff(cursor, direction)

      case Cursor.DOWN =>
        cursorMoveVerticalWithinStaff(cursor, direction)

      case Cursor.CONTROL_DOWN | Cursor.CONTROL_UP =>
        None
    }
  }

  private [this] def cursorMoveEndOfPrevMeasure(cursor: Cursor): Option[Cursor] = {
    val prevMeasureId = cursor.measureId - 1
    val oPrevMeasure = getMeasure(prevMeasureId)
    oPrevMeasure.flatMap(prevMeasure => {
      val newEventId = prevMeasure.events.length
      Some(Cursor(cursor.sectionIds, cursor.staff, prevMeasureId, newEventId, cursor.barLine))
    })
  }

  private [this] def cursorMoveSameEventOfPrevMeasure(cursor: Cursor): Option[Cursor] = {
    val prevMeasureId = cursor.measureId - 1
    val oPrevMeasure = getMeasure(prevMeasureId)
    oPrevMeasure.flatMap(prevMeasure => {
      val newEventId = math.min(cursor.measureEventId.eventId, prevMeasure.events.length)
      Some(Cursor(cursor.sectionIds, cursor.staff, prevMeasureId, newEventId, cursor.barLine))
    })
  }

  private [this] def cursorMoveSameEventOfNextMeasure(cursor: Cursor): Option[Cursor] = {
    val nextMeasureId = cursor.measureId + 1
    val oNextMeasure = getMeasure(nextMeasureId)
    oNextMeasure.flatMap(nextMeasure => {
      val newEventId = math.min(cursor.measureEventId.eventId, nextMeasure.events.length)
      Some(Cursor(cursor.sectionIds, cursor.staff, nextMeasureId, newEventId, cursor.barLine))
    })
  }

  /**
    * This is a necessary evil right now. It moves the cu
    * @param cursor in
    * @return
    */
  /*
  protected def trueUpCursorForward(cursor: Cursor): Cursor = {
    var measureEventId = cursor.measureEventId
    var looping = true
    while(looping){
      val oMeasure = getMeasure(measureEventId.measureId)
      if(oMeasure.isDefined) {
        val measure = oMeasure.get
        if (measureEventId.eventId > measure.events.length) {
          measureEventId = MeasureEventId(measureEventId.measureId + 1, measureEventId.eventId - measure.events.length)
        } else
          looping = false
      } else
        looping = false
    }
    new Cursor(cursor.sectionIds, cursor.staff, measureEventId, cursor.barLine)
  }*/

  def getStaffEventIdFromMeasureEventId(meid: MeasureEventId): Int = {
    val oMeasure = getMeasure(meid.measureId)
    if(oMeasure.isDefined){
      val measure = oMeasure.get
      meid.eventId + measure.firstEventIndex
    } else
      0
  }

  def getLastMeasureEventId: MeasureEventId = {
    val measureId = numMeasures - 1
    val measure = getMeasureUnsafe(measureId)
    val eventId = math.max(measure.numEvents - 1, 0)
    MeasureEventId(measureId, eventId)
  }

  def getMeasureEventIdFromStaffEventId(eventId: Int): MeasureEventId = {
    //the dumb way
    var measureId = 0
    var i = 0
    var found = false
    var out: Option[MeasureEventId] = None
    while(!found && measureId < numMeasures){
      var j = 0
      val measure = getMeasureUnsafe(measureId)
      while(!found && j < measure.numEvents){
        if(i == eventId){
          out = Some(MeasureEventId(measureId, j))
          found = true
        }

        j += 1
        i += 1
      }

      measureId += 1
    }

    out.getOrElse(getLastMeasureEventId)
  }
}
