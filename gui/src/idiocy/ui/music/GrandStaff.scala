package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.music.event._
import idiocy.ui._
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.music.key.{IntNaturalPitchClass, Key, SPNPitch}
import idiocy.ui.clipboard._
import idiocy.ui.data.TimeSig
import idiocy.ui.utils.ArrayUtils

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

object GrandStaff{
  val numInvisibleSpaces = 2

  val UpperRestBarLine: Int = 6
  val LowerRestBarLine: Int = -6

  val MaxBarLine: Int = 10 + numInvisibleSpaces
  val MinBarLine: Int = -10 - numInvisibleSpaces

  def getIntNaturalPitchClassFromBarLoc(barLoc: Int): IntNaturalPitchClass = {
    IntNaturalPitchClass.C + barLoc
  }

  /**
    * Merge rest events vertically. If there are two rests on the same staff, then replace them with a single rest.
    * If there are two rest events on both staffs, replace them with a single full rest
    * @param eventSet the event set to fix.
    * @return
    */
  def fixUpRests(eventSet: MusicEventSet): MusicEventSet = {
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
        case note: MusicNote =>
          if (note.barLocation > 0)
            upperNotes = upperNotes :+ i
          else if (note.barLocation < 0)
            lowerNotes = lowerNotes :+ i
          else
            centralNotes = centralNotes :+ i

        case rest: PartialRest =>
          if (rest.barLocation > 0)
            upperPartialRests = upperPartialRests :+ i
          else if (rest.barLocation < 0)
            lowerPartialRests = lowerPartialRests :+ i
          else
            centralPartialRests = centralPartialRests :+ i

        case rest: FullRest =>
          if (rest.barLocation > 0)
            upperFullRests = upperFullRests :+ i
          else
            lowerFullRests = lowerFullRests :+ i

        case span: MusicNoteSpan =>
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
        newTheseEvents = newTheseEvents :+ FullRest(eventSet.lengthPips,
          GrandStaff.UpperRestBarLine)
      }
    }

    if(lowerNotes.isEmpty && lowerSpans.isEmpty && centralNotes.isEmpty && centralSpans.isEmpty){
      if(!lowerPartialRests.isEmpty){
        //it has only partial rests. strip them out, replace with a full
        newTheseEvents = newTheseEvents.removeIndexes(lowerPartialRests)
      }

      if(lowerFullRests.isEmpty){
        newTheseEvents = newTheseEvents :+ FullRest(eventSet.lengthPips,
          GrandStaff.LowerRestBarLine)
      }
    }

    newTheseEvents
  }

  def fixUp(events: Array[MusicEventSet]) :Array[MusicEventSet] = {
    //if there are rests missing in, add them in
    val newEvents = events.map(e => GrandStaff.fixUpRests(e))

    //now smoosh
    MusicEventSet.smoosh(newEvents)
  }

  def generateMeasures(events: Array[MusicEventSet]): Array[GrandStaffMeasure] = {
    if(events.isEmpty)
      Array(GrandStaffMeasure(TimeSig.timeSig44, Array(), 0))
    else {
      var timeSig = TimeSig.timeSig44
      var key = Key.CMajor

      //peek the first event, see if we have a timesig and/or key
      val firstEvents = events(0)
      var eventIdx = 0
      while(eventIdx < firstEvents.length){
        firstEvents(eventIdx) match {
          case timeSigEvent: TimeSigEvent =>
            timeSig = timeSigEvent.timeSig
          case keySignatureEvent: KeySignatureEvent =>
            key = keySignatureEvent.key
          case _ =>
        }
        eventIdx += 1
      }

      var eventSetIdx = 0
      var out = ArrayBuffer[GrandStaffMeasure]()
      var thisMeasureEventSets = ArrayBuffer[MusicEventSet]()
      var thisMeasureEventSetIndexes = ArrayBuffer[Int]()
      //var thisMeasureFirstEventIndex = 0
      var measureLengthPips = timeSig.measureLengthPips
      while(eventSetIdx < events.length){
        val theseEvents = events(eventSetIdx)

        var eventIdx = 0
        var newTimeSig: Option[TimeSig] = None
        while(eventIdx < theseEvents.length){
          theseEvents(eventIdx) match {
            case timeSigEvent: TimeSigEvent =>
              newTimeSig = Some(timeSigEvent.timeSig)
            case _ =>
          }
          eventIdx += 1
        }

        newTimeSig.foreach(ts=>{
          out += GrandStaffMeasure(timeSig, thisMeasureEventSets.toArray, thisMeasureEventSetIndexes.head)
          timeSig = ts
          measureLengthPips = timeSig.measureLengthPips
          thisMeasureEventSets.clear()
          thisMeasureEventSetIndexes.clear()
        })

        thisMeasureEventSets += theseEvents
        thisMeasureEventSetIndexes += eventSetIdx

        measureLengthPips -= theseEvents.lengthPips
        if(measureLengthPips <= 0){
          out += GrandStaffMeasure(timeSig, thisMeasureEventSets.toArray, thisMeasureEventSetIndexes.head)
          measureLengthPips = timeSig.measureLengthPips
          thisMeasureEventSets.clear()
          thisMeasureEventSetIndexes.clear()
        }
        eventSetIdx += 1
      }

      //end stub
      if(thisMeasureEventSets.nonEmpty)
        out += GrandStaffMeasure(timeSig, thisMeasureEventSets.toArray, thisMeasureEventSetIndexes.head)

      out.toArray
    }
  }
}

class GrandStaff(val events: Array[MusicEventSet], val measures: Array[GrandStaffMeasure]) extends Staff {

  val lengthPips: Long = events.foldLeft(0L)((b, es)=>b + es.lengthPips)

  def getSPNPitchFromBarLoc(barLoc: Int, accidental: Int, key: Key): SPNPitch = {
    //first, get the base natural pitch class of this bar, unmodified by any key
    val inpc = GrandStaff.getIntNaturalPitchClassFromBarLoc(barLoc)
    //these are the pitch classes of this key
    val keyCPCs = key.compositePitchClasses
    //find the natural pitch class in the key
    val idx = keyCPCs.indexWhere(c=>c.intNaturalPitchClass == inpc)
    val keyCPC = keyCPCs(idx)
    //now apply the accidental.
    val outCPC = accidental match {
      case MusicNote.NoAccidental => keyCPC
      case MusicNote.NaturalAccidental => keyCPC.`♮`
      case MusicNote.FlatAccidental => keyCPC.`♭`
      case MusicNote.SharpAccidental => keyCPC.`♯`
      case MusicNote.DoubleFlatAccidental => keyCPC.`𝄫`
      case MusicNote.DoubleSharpAccidental => keyCPC.`𝄪`
    }

    val octaveNumber = (barLoc + 8*4)/8
    SPNPitch(outCPC, octaveNumber)
  }

  override def getClipboardEventsFromSelection(selection: EventSelection): Array[ClipboardEventSet] = {
    val out = ArrayBuffer[ClipboardEventSet]()
    var eventId = getStaffEventIdFromMeasureEventId(selection.meidL)
    val eventIdR = getStaffEventIdFromMeasureEventId(selection.meidR)
    var key = getKey(eventId)
    while(eventId < eventIdR){
      val eventSet = events(eventId)
      val thisOut = ArrayBuffer[ClipboardEvent]()

      eventSet.events.foreach({
        case rest: FullRest =>
          thisOut += new ClipboardFullRest(rest.lengthPips)
        case note: MusicNote =>
          thisOut += new ClipboardNote(note.lengthPips, getSPNPitchFromBarLoc(note.barLocation, note.accidental, key))
        case rest: PartialRest =>
          thisOut += new ClipboardPartialRest(rest.lengthPips, getSPNPitchFromBarLoc(rest.barLocation, 0, key))
        case span: MusicNoteSpan =>
          thisOut += new ClipboardSpan(span.lengthPips, getSPNPitchFromBarLoc(span.barLocation, span.accidental, key))
        case keySignatureEvent: KeySignatureEvent => key = keySignatureEvent.key
      })
      var ces = new ClipboardEventSet(thisOut.toArray, eventSet.lengthPips)
      ces = ces.fixUpRests
      out += ces
      eventId += 1
    }
    out.toArray
  }

  def getClipboardEventsFromSelection(selection: MeasureSelection): Array[Array[ClipboardEventSet]] = {
    ???
    /*
    val out = ArrayBuffer[ClipboardMeasure]()
    var measureId = selection.measureL
    while(measureId <= selection.measureR && measureId < measures.length - 1){
      val measure = measures(measureId)
      val ces = measure.getClipboardEventSets(0, measure.events.length)
      out += new ClipboardMeasure(ces, ClipboardMeasure.MEASURE_TYPE_FULL, ClipboardMeasure.MEASURE_TYPE_FULL)
      measureId += 1
    }
    out.toArray
     */
  }

  override def lineHeightPixels(displayParams: PieceDisplayParams): Int = {
    displayParams.staffLineSeparationPixels * (10 + GrandStaff.numInvisibleSpaces * 2) +
      displayParams.staffLineWidthPixels * (9 + GrandStaff.numInvisibleSpaces * 2)
  }

  def bar0OffsPixels(displayParams: PieceDisplayParams): Int = {
    displayParams.staffLineSeparationPixels * (5 + GrandStaff.numInvisibleSpaces) +
      displayParams.staffLineWidthPixels * (4 + GrandStaff.numInvisibleSpaces)
  }

  def getMeasure(measureId: Int): Option[Measure] = {
    if(measureId >= measures.length)
      None
    else
      Some(measures(measureId))
  }

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

      val measureOffset = new Point(offset.x + xOffs, offset.y)
      val measureCanvas = new Dimension(measureWidth, canvas.height)
      measure.render(graphics, staffId, measureId, selection, measureOffset, bar0Offs,
        measureCanvas, displayKey, displayParams)

      if(cursor.staff == staffId && cursor.measureId == measureId)
        measure.renderInsertCursor(graphics, measureOffset, bar0Offs, measureCanvas, cursor, displayParams)

      xOffs += measureWidth
      displayKey = false
      measureId += 1
    }
  }

  override def backspaceAtCursor(cursor: Cursor): (Staff, Cursor) = {
    ???
    /*
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
      newEvents = DisplayEventSet.smoosh(newEvents)

      (new DisplayGrandStaffMeasure(timeSig, bpm, key, newEvents), cursor.prevEvent)
    }
    else {
      (this, cursor)
    }
     */
  }

  private [this] def stripOutBarLine(events: Array[MusicEventSet],
                                     from: Int,
                                     barLocation: Int,
                                     lengthPips: Long): Array[MusicEventSet] = {
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
          case note: MusicNote =>
            theseEvents = theseEvents.removeIndex(eventIndex)
            if(!firstIteration)
              remainingLength += (note.lengthPips - theseEventsLength)

          case fullRest: FullRest =>
            if((barLocation >= 0 && fullRest.barLocation > 0) || (barLocation <= 0 && fullRest.barLocation < 0) || (barLocation == 0))
              theseEvents = theseEvents.removeIndex(eventIndex)
          case _: PartialRest =>
            theseEvents = theseEvents.removeIndex(eventIndex)
          case _: MusicNoteSpan =>
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

  override def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (Staff, Cursor) = {
    //this is all overwrite mode
    val cursorEventId = getStaffEventIdFromMeasureEventId(cursor.measureEventId)
    val newEvents: Array[MusicEventSet] = if(cursorEventId >= events.length){
      events :+ new MusicEventSet(Array[MusicEvent](
        FullRest(lengthPips, GrandStaff.UpperRestBarLine),
        FullRest(lengthPips, GrandStaff.LowerRestBarLine)),
        lengthPips)
    } else {

      var newEvents = stripOutBarLine(events, cursorEventId, cursor.barLine, lengthPips)

      //now put in the rest, possibly splitting
      newEvents = MusicEventSet.addInLongRest(newEvents, cursorEventId, lengthPips, cursor.barLine)

      //fix up any problems
      newEvents = GrandStaff.fixUp(newEvents)

      newEvents
    }
    (new GrandStaff(newEvents, GrandStaff.generateMeasures(newEvents)), cursor.nextEvent)
  }

  override def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int, cursor: Cursor): (Staff, Cursor) = {
    //this is all overwrite mode
    val cursorEventId = getStaffEventIdFromMeasureEventId(cursor.measureEventId)
    val newEvents: Array[MusicEventSet] =
      if(cursorEventId >= events.length){
        if(cursor.barLine == 0)
          events :+ new MusicEventSet(Array[MusicEvent](
            new MusicNote(lengthPips, cursor.barLine, noteIdModifier)
          ), lengthPips)
        else if(cursor.barLine < 0)
          events :+ new MusicEventSet(Array[MusicEvent](
            FullRest(lengthPips, GrandStaff.UpperRestBarLine),
            MusicNote(lengthPips, cursor.barLine, noteIdModifier)
          ), lengthPips)
        else
          events :+ new MusicEventSet(Array[MusicEvent](
            MusicNote(lengthPips, cursor.barLine, noteIdModifier),
            FullRest(lengthPips, GrandStaff.LowerRestBarLine)
          ), lengthPips)
      } else {
        //strip out whatever is on this bar line, possibly including future notes
        var newEvents = stripOutBarLine(events, cursorEventId, cursor.barLine, lengthPips)

        //now put in the note, possibly splitting
        newEvents = MusicEventSet.addInLongNote(newEvents, cursorEventId,
          new MusicNote(lengthPips, cursor.barLine, noteIdModifier))

        //fix up any problems
        newEvents = GrandStaff.fixUp(newEvents)

        newEvents
      }
    val newGS = new GrandStaff(newEvents, GrandStaff.generateMeasures(newEvents))
    val newCursor = newGS.trueUpCursor(cursor.nextEvent)
    (newGS, newCursor)
  }

  override def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Staff, Cursor) = {
???
    /*
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
    } */
  }

  override def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Staff, Cursor) = {
    ???
    /*
    val newStaff = deleteMeasures(cursor.measure, numMeasures)
    val dgs = newStaff.asInstanceOf[DisplayGrandStaff]
    val newMeasureId = if(dgs.measures.length >= cursor.measure) dgs.measures.length - 1 else cursor.measure
    val newEventId = if(dgs.measures(newMeasureId).events.length > cursor.event)
      dgs.measures(newMeasureId).events.length
    else
      cursor.event
    (newStaff, Cursor(cursor.staff, newMeasureId, newEventId, cursor.barLine))
     */
  }

  /*

  override def deleteMeasures(measureId: Int, numMeasures: Int): DisplayStaff = {
    var newMeasures = ArrayUtils.removeIndexes(measures, measureId, numMeasures)
    if(newMeasures.isEmpty){
      val oldFirstMeasure = measures(0)
      newMeasures = Array(oldFirstMeasure.cloneEmpty)
    }
    new DisplayGrandStaff(newMeasures)
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
   */
  override def numMeasures: Int = measures.length

  override def cursorMoveStaff(cursor: Cursor, direction: Int): Option[Cursor] = direction match {
    case Cursor.UP =>
      if(cursor.barLine < GrandStaff.MaxBarLine) Some(cursor.upABarLine) else None
    case Cursor.DOWN =>
      if(cursor.barLine > GrandStaff.MinBarLine) Some(cursor.downABarLine) else None
  }

  override def getTopCursor(sectionIds: Array[Int], staffId: Int, meid: MeasureEventId): Cursor = {
    val m = math.min(meid.measureId, measures.length-1)
    if(m < meid.measureId)
      Cursor(sectionIds, staffId, m, 0, GrandStaff.MaxBarLine)
    else {
      val e = math.min(meid.eventId, measures(m).events.length)
      Cursor(sectionIds, staffId, m, e, GrandStaff.MaxBarLine)
    }
  }

  override def getBottomCursor(sectionIds: Array[Int], staffId: Int, meid: MeasureEventId): Cursor = {
    val m = math.min(meid.measureId, measures.length-1)
    if(m < meid.measureId){
      Cursor(sectionIds, staffId, m, 0, GrandStaff.MinBarLine)
    } else {
      val e = math.min(meid.eventId, measures(m).events.length)
      Cursor(sectionIds, staffId, m, e, GrandStaff.MinBarLine)
    }
  }

  override def getLeftCursor(sectionIds: Array[Int], staffId: Int, measureId: Int, barLine: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    Cursor(sectionIds, staffId, m, 0, barLine)
  }

  override def getRightCursor(sectionIds: Array[Int], staffId: Int, measureId: Int, barLine: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    Cursor(sectionIds, staffId, m, measures(m).events.length, barLine)
  }

  override def getTopLeftCursor(sectionIds: Array[Int], staffId: Int, measureId: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    Cursor(sectionIds, staffId, m, 0, GrandStaff.MinBarLine)
  }

  override def getBottomRightCursor(sectionIds: Array[Int], staffId: Int, measureId: Int): Cursor = {
    val m = math.min(measureId, measures.length-1)
    Cursor(sectionIds, staffId, m, measures(m).events.length, GrandStaff.MaxBarLine)
  }

  override def getConstrainedCursor(sectionIds: Array[Int], staffId: Int, meid: MeasureEventId, barLine: Int): Cursor = {
    val m = math.min(meid.measureId, measures.length-1)
    val e = math.min(measures(m).events.length, meid.eventId)
    val b = math.min(GrandStaff.MaxBarLine, math.max(GrandStaff.MinBarLine, barLine))
    Cursor(sectionIds, staffId, m, e, b)
  }
}
