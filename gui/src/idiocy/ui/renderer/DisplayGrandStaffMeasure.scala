package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.music.key.{IntNaturalPitchClass, Key, SPNPitch}
import idiocy.ui._
import idiocy.ui.clipboard._
import idiocy.ui.data.TimeSig
import idiocy.ui.utils.ArrayUtils

import scala.collection.mutable.ArrayBuffer

object DisplayGrandStaffMeasure{
  val UpperRestBarLine: Int = 6
  val LowerRestBarLine: Int = -6

  val numInvisibleSpaces = 2

  /**
    * Merge rest events vertically. If there are two rests on the same staff, then replace them with a single rest.
    * If there are two rest events on both staffs, replace them with a single full rest
    * @param eventSet the event set to fix.
    * @return
    */
  def fixUpRests(eventSet: DisplayEventSet): DisplayEventSet = {
    var upperFullRests = Array[Int]()
    var lowerFullRests = Array[Int]()
    var upperPartialRests = Array[Int]()
    var upperSpans = Array[Int]()
    var upperNotes = Array[Int]()
    var lowerPartialRests = Array[Int]()
    var lowerSpans = Array[Int]()
    var lowerNotes = Array[Int]()
    var centralNotes = Array[Int]()
    var centralPartialRests = Array[Int]()
    var centralSpans = Array[Int]()

    var i = 0
    while(i < eventSet.events.length) {
      val event = eventSet.events(i)
      event match {
        case note: DisplayNote =>
          if (note.barLocation > 0)
            upperNotes = upperNotes :+ i
          else if (note.barLocation < 0)
            lowerNotes = lowerNotes :+ i
          else
            centralNotes = centralNotes :+ i

        case rest: DisplayPartialRest =>
          if (rest.barLocation > 0)
            upperPartialRests = upperPartialRests :+ i
          else if (rest.barLocation < 0)
            lowerPartialRests = lowerPartialRests :+ i
          else
            centralPartialRests = centralPartialRests :+ i

        case rest: DisplayFullRest =>
          if (rest.barLocation > 0)
            upperFullRests = upperFullRests :+ i
          else
            lowerFullRests = lowerFullRests :+ i

        case span: DisplaySpan =>
          if (span.barLocation > 0)
            upperSpans = upperSpans :+ i
          else if (span.barLocation < 0)
            lowerSpans = lowerSpans :+ i
          else
            centralSpans = centralSpans :+ i
      }
      i += 1
    }

    var newTheseEvents = eventSet
    //upper
    if(upperNotes.isEmpty && upperSpans.isEmpty && centralNotes.isEmpty && centralSpans.isEmpty){
      if(!upperPartialRests.isEmpty){
        //it has only partial rests. strip them out, replace with a full
        newTheseEvents = newTheseEvents.removeIndexes(upperPartialRests)
      }

      //no upper rests; put a full rest in
      if(upperFullRests.isEmpty){
        newTheseEvents = newTheseEvents :+ DisplayFullRest(eventSet.lengthPips,
          DisplayGrandStaffMeasure.UpperRestBarLine)
      }
    }

    if(lowerNotes.isEmpty && lowerSpans.isEmpty && centralNotes.isEmpty && centralSpans.isEmpty){
      if(!lowerPartialRests.isEmpty){
        //it has only partial rests. strip them out, replace with a full
        newTheseEvents = newTheseEvents.removeIndexes(lowerPartialRests)
      }

      if(lowerFullRests.isEmpty){
        newTheseEvents = newTheseEvents :+ DisplayFullRest(eventSet.lengthPips,
          DisplayGrandStaffMeasure.LowerRestBarLine)
      }
    }

    newTheseEvents
  }

  def fixUp(events: Array[DisplayEventSet]) :Array[DisplayEventSet] = {
    //if there are rests missing in, add them in
    val newEvents = events.map(e => DisplayGrandStaffMeasure.fixUpRests(e))

    //now smoosh
    DisplayStaff.smoosh(newEvents)
  }

  def getINPCFromBarLoc(barLoc: Int): IntNaturalPitchClass = {
    IntNaturalPitchClass.C + barLoc
  }
}

final class DisplayGrandStaffMeasure(val timeSig: TimeSig,
                               val bpm: Float,
                               val key: Key,
                               val events: Array[DisplayEventSet]
                               ) extends DisplayMeasure {

  def backspace(cursor: Cursor): (DisplayGrandStaffMeasure, Cursor) = {
    if(cursor.event > 0){
      var eventSet = events(cursor.event - 1)

      var eventIndexes = eventSet.findEventIndexes(cursor.barLine)
      while(eventIndexes.length > 0){
        eventSet = eventSet.removeIndex(eventIndexes(0))
        eventIndexes = eventSet.findEventIndexes(cursor.barLine)
      }

      eventSet = DisplayGrandStaffMeasure.fixUpRests(eventSet)

      var newEvents = if(eventSet.length > 0)
        ArrayUtils.replaceElem(events, cursor.event - 1, eventSet)
      else
        ArrayUtils.removeIndex(events, cursor.event - 1)

      //now smoosh
      newEvents = DisplayStaff.smoosh(newEvents)

      (new DisplayGrandStaffMeasure(timeSig, bpm, key, newEvents), cursor.prevEvent)
    }
    else {
      (this, cursor)
    }
  }


  /**
    * Given an event set, how long it was and how long it will be, split it into two event sets, the first with the new
    * size and the second with the left-over size. Notes are split into spans, rests into sub-rests
    *
    * @param theseEvents events to split
    * @param oldLengthPips the old length in pips
    * @param newLengthPips the new length in pips
    * @return
    */
  private [this] def splitEvents(theseEvents: DisplayEventSet,
                                 oldLengthPips: Long,
                                 newLengthPips: Long): (DisplayEventSet, DisplayEventSet) = {
    val stubLengthPips = oldLengthPips - newLengthPips
    var j = 0
    var outFirst = new DisplayEventSet(Array(), newLengthPips)
    var outSecond = new DisplayEventSet(Array(), stubLengthPips)
    while(j < theseEvents.length){
      val thisEvent = theseEvents(j)
      thisEvent match {
        case displayNote: DisplayNote =>
          outFirst = outFirst :+ displayNote
          outSecond = outSecond :+ DisplaySpan(stubLengthPips, displayNote.barLocation, displayNote.accidental)
        case displayRest: DisplayPartialRest =>
          outFirst = outFirst :+ DisplayPartialRest(newLengthPips, displayRest.barLocation)
          outSecond = outSecond :+ DisplayPartialRest(stubLengthPips, displayRest.barLocation)
        case displayRest: DisplayFullRest =>
          outFirst = outFirst :+ DisplayFullRest(newLengthPips, displayRest.barLocation)
          outSecond = outSecond :+ DisplayFullRest(stubLengthPips, displayRest.barLocation)
        case displaySpan: DisplaySpan =>
          outFirst = outFirst :+ DisplaySpan(newLengthPips, displaySpan.barLocation, displaySpan.accidental)
          outSecond = outSecond :+ DisplaySpan(stubLengthPips, displaySpan.barLocation, displaySpan.accidental)
      }
      j += 1
    }
    (outFirst, outSecond)
  }


  private [this] def addInLongNote(events: Array[DisplayEventSet], from: Int, displayNote: DisplayNote): Array[DisplayEventSet] = {
    val unchangedBeginStub = ArrayUtils.subRange(events, 0, from)
    var out = unchangedBeginStub
    var remainingLength = displayNote.lengthPips
    var i = from

    var theseEvents = events(i)
    val theseEventsLength = theseEvents.lengthPips

    if(remainingLength < theseEventsLength){
      //split
      val (new1, new2) = splitEvents(theseEvents, theseEventsLength, remainingLength)
      out = out :+
        (new1 :+ displayNote) :+
        (new2 :+ DisplayPartialRest(theseEventsLength - remainingLength, displayNote.barLocation))
      remainingLength = 0
      i += 1
    } else {
      theseEvents = theseEvents :+ displayNote
      out = out :+ theseEvents
      remainingLength -= theseEventsLength
      i += 1

      while (i < events.length && remainingLength > 0) {
        val theseEvents = events(i)
        val theseEventsLength = theseEvents.lengthPips
        if (theseEventsLength <= remainingLength) {
          remainingLength -= theseEventsLength
          out = out :+ (theseEvents :+ DisplaySpan(theseEventsLength, displayNote.barLocation, displayNote.accidental))
        } else {
          val (new1, new2) = splitEvents(theseEvents, theseEventsLength, remainingLength)
          remainingLength = 0
          out = out :+ (new1 :+ DisplaySpan(remainingLength, displayNote.barLocation, displayNote.accidental)) :+
            (new2 :+ DisplayPartialRest(theseEventsLength - remainingLength, displayNote.barLocation))
        }

        i += 1
      }
    }

    if (i < events.length)
      out = out ++ ArrayUtils.subRange(events, i, events.length)
    else if(remainingLength > 0){
      out = out :+ new DisplayEventSet(Array(DisplaySpan(remainingLength, displayNote.barLocation, displayNote.accidental)), remainingLength)
    }
    out
  }

  def addInLongRest(newEvents: Array[DisplayEventSet], from: Int, lengthPips: Long, barLocation: Int): Array[DisplayEventSet] = {
    val unchangedBeginStub = ArrayUtils.subRange(events, 0, from)
    var out = unchangedBeginStub
    var remainingLength = lengthPips
    var i = from

    val theseEvents = events(i)
    val theseEventsLength = theseEvents.lengthPips

    if(remainingLength < theseEventsLength){
      //split
      val (new1, new2) = splitEvents(theseEvents, theseEventsLength, remainingLength)
      out = out :+
        (new1 :+ DisplayPartialRest(remainingLength, barLocation)) :+ new2
      remainingLength = 0
      i += 1
    } else {
      while (i < events.length && remainingLength > 0) {
        val theseEvents = events(i)
        val theseEventsLength = theseEvents.lengthPips
        if (theseEventsLength <= remainingLength) {
          remainingLength -= theseEventsLength
          out = out :+ (theseEvents :+ DisplayPartialRest(theseEventsLength, barLocation))
        } else {
          val (new1, new2) = splitEvents(theseEvents, theseEventsLength, remainingLength)
          remainingLength = 0
          out = out :+ (new1 :+ DisplayPartialRest(remainingLength, barLocation)) :+ new2
        }

        i += 1
      }
    }

    if (i < events.length)
      out = out ++ ArrayUtils.subRange(events, i, events.length)
    else if(remainingLength > 0){
      out = out :+ new DisplayEventSet(Array(DisplayPartialRest(remainingLength, barLocation)), remainingLength)
    }
    out
  }

  private [this] def stripOutBarLine(events: Array[DisplayEventSet],
                                     from: Int,
                                     barLocation: Int,
                                     lengthPips: Long): Array[DisplayEventSet] = {
    val unchangedBeginStub = ArrayUtils.subRange(events, 0, from)
    var out = unchangedBeginStub
    var remainingLength = lengthPips
    var i = from
    var firstIteration = true
    while(i < events.length && remainingLength > 0){
      var theseEvents = events(i)
      val theseEventsLength = theseEvents.lengthPips
      var eventIndexes = theseEvents.findEventIndexes(barLocation)
      var j = 0
      while(j < eventIndexes.length){
        val eventIndex = eventIndexes(j)
        val event = theseEvents(eventIndex)
        event match{
          case note: DisplayNote =>
            theseEvents = theseEvents.removeIndex(eventIndex)
            if(!firstIteration)
              remainingLength += (note.lengthPips - theseEventsLength)

          case fullRest: DisplayFullRest =>
            if((barLocation >= 0 && fullRest.barLocation > 0) || (barLocation <= 0 && fullRest.barLocation < 0) || (barLocation == 0))
              theseEvents = theseEvents.removeIndex(eventIndex)
          case _: DisplayPartialRest =>
            theseEvents = theseEvents.removeIndex(eventIndex)
          case _: DisplaySpan =>
            theseEvents = theseEvents.removeIndex(eventIndex)
          case _ =>
        }
        j += 1
        eventIndexes = theseEvents.findEventIndexes(barLocation)
      }
      remainingLength -= theseEventsLength
      out = out :+ theseEvents
      firstIteration = false
      i += 1
    }

    if(i < events.length)
      out = out ++ ArrayUtils.subRange(events, i, events.length)

    out
  }

  def insertNoteAtCursor(lengthPips: Int,
                         noteIdModifier: Int,
                         cursor: Cursor): (DisplayGrandStaffMeasure, Cursor) = {

    val newEvents: Array[DisplayEventSet] =
      if(cursor.event >= events.length){
        if(cursor.barLine == 0)
          events :+ new DisplayEventSet(Array[DisplayEvent](
            new DisplayNote(lengthPips, cursor.barLine, noteIdModifier)
          ), lengthPips)
        else if(cursor.barLine < 0)
          events :+ new DisplayEventSet(Array[DisplayEvent](
            DisplayFullRest(lengthPips, DisplayGrandStaffMeasure.UpperRestBarLine),
            DisplayNote(lengthPips, cursor.barLine, noteIdModifier)
          ), lengthPips)
        else
          events :+ new DisplayEventSet(Array[DisplayEvent](
            DisplayNote(lengthPips, cursor.barLine, noteIdModifier),
            DisplayFullRest(lengthPips, DisplayGrandStaffMeasure.LowerRestBarLine)
          ), lengthPips)
      } else {
        //strip out whatever is on this bar line, possibly including future notes
        var newEvents = stripOutBarLine(events, cursor.event, cursor.barLine, lengthPips)

        //now put in the note, possibly splitting
        newEvents = addInLongNote(newEvents, cursor.event, new DisplayNote(lengthPips, cursor.barLine, noteIdModifier))

        //fix up any problems
        newEvents = DisplayGrandStaffMeasure.fixUp(newEvents)

        newEvents
      }
    (new DisplayGrandStaffMeasure(timeSig, bpm, key, newEvents), cursor.nextEvent)
  }



  def insertRestAtCursor(lengthPips: Int,
                         cursor: Cursor): (DisplayGrandStaffMeasure, Cursor) = {
    val newEvents: Array[DisplayEventSet] = if(cursor.event >= events.length){
      events :+ new DisplayEventSet(Array[DisplayEvent](
        DisplayFullRest(lengthPips, DisplayGrandStaffMeasure.UpperRestBarLine),
        DisplayFullRest(lengthPips, DisplayGrandStaffMeasure.LowerRestBarLine)),
        lengthPips)
    } else {

      var newEvents = stripOutBarLine(events, cursor.event, cursor.barLine, lengthPips)

      //now put in the rest, possibly splitting
      newEvents = addInLongRest(newEvents, cursor.event, lengthPips, cursor.barLine)

      //fix up any problems
      newEvents = DisplayGrandStaffMeasure.fixUp(newEvents)

      newEvents
    }
    (new DisplayGrandStaffMeasure(timeSig, bpm, key, newEvents), cursor.nextEvent)
  }

  def renderInsertCursor(graphics: Graphics, insertCursor: Cursor, offset: Point, bar0Offs: Int,
                         canvas: Dimension, displayParams: PieceDisplayParams): Unit = {
    val xLoc = xCursorLoc(insertCursor.event, canvas)
    val yLoc = barLineLoc(bar0Offs, insertCursor.barLine, displayParams)

    insertCursor.render(graphics, new Point(offset.x + xLoc, offset.y + yLoc), displayParams)
  }

  private [this] def barLineLoc(bar0Offs: Int, barLocation: Int, displayParams: PieceDisplayParams): Int = {
    bar0Offs - (barLocation * (displayParams.staffLineWidthPixels + displayParams.staffLineSeparationPixels) / 2)
  }

  private [this] def xEventLoc(eventIdx: Int, canvas: Dimension): Int = {
    noteSpace(canvas) * (eventIdx + 1)
  }

  private [this] def xCursorLoc(eventIdx: Int, canvas: Dimension): Int = {
    val ns = noteSpace(canvas)
    xEventLoc(eventIdx, canvas) - ns / 2
  }

  private [this] def renderSelection(graphics: Graphics,
                                     staffId: Int,
                                     measureId: Int,
                                     selection: Selection,
                                     offset: Point,
                                     bar0Offs: Int,
                                     canvas: Dimension,
                                     displayParams: PieceDisplayParams): Unit = {
    selection match {
      case _: NoSelection =>
      case measureSelection: MeasureSelection =>
        if(measureSelection.staffT <= staffId && measureSelection.staffB >= staffId && measureSelection.measureL <= measureId &&
          measureSelection.measureR >= measureId) {
          graphics.setColor(GlobalUISettings.palette.selectionBgColor)
          graphics.fillRect(offset.x, offset.y, canvas.width, canvas.height)
        }
      case eventSelection: EventSelection =>
        if(eventSelection.staff == staffId){
          if(eventSelection.measureL <= measureId && eventSelection.measureR >= measureId){
            val l = if(eventSelection.measureL == measureId)
              xCursorLoc(eventSelection.eventL, canvas)
            else
              0
            val r = if(eventSelection.measureR == measureId)
              xCursorLoc(eventSelection.eventR, canvas)
            else
              canvas.width

            graphics.setColor(GlobalUISettings.palette.selectionBgColor)
            graphics.fillRect(offset.x + l, offset.y, r-l, canvas.height
            )
          }
        }
    }
  }

  override def render(graphics: Graphics,
                      staffId: Int,
                      measureId: Int,
                      selection: Selection,
                      offset: Point, bar0Offs: Int, canvas: Dimension,
                      displayKey: Boolean, displayParams: PieceDisplayParams): Unit = {
    renderSelection(graphics, staffId, measureId, selection, offset, bar0Offs, canvas, displayParams)

    renderBarLines(graphics, new Point(offset.x, offset.y + barLineLoc(bar0Offs, 10, displayParams)), canvas, 5, displayParams)
    renderBarLines(graphics, new Point(offset.x, offset.y + barLineLoc(bar0Offs, -2, displayParams)), canvas, 5, displayParams)
    graphics.drawLine(offset.x + canvas.width - 1, offset.y + barLineLoc(bar0Offs, 10, displayParams),
      offset.x + canvas.width - 1, offset.y + barLineLoc(bar0Offs, -10, displayParams))

    renderEvents(events, graphics, offset, bar0Offs, canvas, displayParams)
    graphics.setColor(GlobalUISettings.palette.staffColor)

  }

  private def renderEvents(events: Array[DisplayEventSet], graphics: Graphics, offset: Point, bar0Offs: Int,
                           canvas: Dimension, displayParams: PieceDisplayParams): Unit = {
    var i = 0
    while (i < events.length) {
      var j = 0
      val thisEvents = events(i)
      while(j < thisEvents.length) {
        thisEvents(j) match {
          case displayNote: DisplayNote =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = barLineLoc(bar0Offs, displayNote.barLocation, displayParams)
            val notePoint = new Point(offset.x + xOffs, offset.y + yOffs)

            if (displayNote.barLocation == 0) {
              graphics.setColor(GlobalUISettings.palette.staffColor)
              graphics.drawLine(notePoint.x - 4, notePoint.y, notePoint.x + 4, notePoint.y)
            } else if (displayNote.barLocation > 11) {
              var i = 12
              graphics.setColor(GlobalUISettings.palette.staffColor)
              while (i <= displayNote.barLocation) {
                val y = offset.y + barLineLoc(bar0Offs, i, displayParams)
                graphics.drawLine(notePoint.x - 4, y, notePoint.x + 4, y)
                i += 2
              }
            } else if (displayNote.barLocation < -11) {
              var i = -12
              graphics.setColor(GlobalUISettings.palette.staffColor)
              while (i >= displayNote.barLocation) {
                val y = offset.y + barLineLoc(bar0Offs, i, displayParams)
                graphics.drawLine(notePoint.x - 4, y, notePoint.x + 4, y)
                i -= 2
              }
            }

            val stemUp = displayNote.barLocation < 6 && displayNote.barLocation > 0 ||
              displayNote.barLocation < -6
            displayNote.render(graphics, notePoint, stemUp, displayParams)
          case displayRest: DisplayPartialRest =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = barLineLoc(bar0Offs, displayRest.barLocation, displayParams)
            val restPoint = new Point(offset.x + xOffs, offset.y + yOffs)
            displayRest.render(graphics, restPoint, displayParams)
          case displayFullRest: DisplayFullRest =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = barLineLoc(bar0Offs, displayFullRest.barLocation, displayParams)
            val restPoint = new Point(offset.x + xOffs, offset.y + yOffs)
            displayFullRest.render(graphics, restPoint, displayParams)
          case displaySpan: DisplaySpan =>
            val xOffs = xEventLoc(i, canvas)
            val yOffs = barLineLoc(bar0Offs, displaySpan.barLocation, displayParams)
            val spanPoint = new Point(offset.x + xOffs, offset.y + yOffs)
            displaySpan.render(graphics, spanPoint, displayParams)
        }
        j += 1
      }
      i += 1
    }
  }

  def cloneEmpty: DisplayGrandStaffMeasure = new DisplayGrandStaffMeasure(timeSig, bpm, key, Array())
  def cloneNewEvents(events: Array[DisplayEventSet]) : DisplayGrandStaffMeasure =
    new DisplayGrandStaffMeasure(timeSig, bpm, key, events)

  def getClipboardEventSets(firstEventId: Int, lastEventId: Int): Array[ClipboardEventSet] = {
    val out = ArrayBuffer[ClipboardEventSet]()
    var eventId = firstEventId
    while(eventId < lastEventId){
      val eventSet = events(eventId)
      var ces = new ClipboardEventSet(eventSet.events.map({
        case rest: DisplayFullRest => new ClipboardFullRest(rest.lengthPips)
        case note: DisplayNote => new ClipboardNote(note.lengthPips, getSPNPitchFromBarLoc(note.barLocation, note.accidental))
        case rest: DisplayPartialRest => new ClipboardPartialRest(rest.lengthPips, getSPNPitchFromBarLoc(rest.barLocation, 0))
        case span: DisplaySpan => new ClipboardSpan(span.lengthPips, getSPNPitchFromBarLoc(span.barLocation, span.accidental))

      }), eventSet.lengthPips)
      ces = ces.fixUpRests
      out += ces
      eventId += 1
    }
    out.toArray
  }

  def getSPNPitchFromBarLoc(barLoc: Int, accidental: Int): SPNPitch = {
    //first, get the base natural pitch class of this bar, unmodified by any key
    val inpc = DisplayGrandStaffMeasure.getINPCFromBarLoc(barLoc)
    //these are the pitch classes of this key
    val keyCPCs = key.compositePitchClasses
    //find the natural pitch class in the key
    val idx = keyCPCs.indexWhere(c=>c.intNaturalPitchClass == inpc)
    val keyCPC = keyCPCs(idx)
    //now apply the accidental.
    val outCPC = accidental match {
      case DisplayNote.NoAccidental => keyCPC
      case DisplayNote.NaturalAccidental => keyCPC.`♮`
      case DisplayNote.FlatAccidental => keyCPC.`♭`
      case DisplayNote.SharpAccidental => keyCPC.`♯`
      case DisplayNote.DoubleFlatAccidental => keyCPC.`𝄫`
      case DisplayNote.DoubleSharpAccidental => keyCPC.`𝄪`
    }

    val octaveNumber = (barLoc + 8*4)/8
    SPNPitch(outCPC, octaveNumber)
  }
}
