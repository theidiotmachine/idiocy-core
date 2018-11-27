package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.music.event._
import idiocy.ui._
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.music.key.{IntNaturalPitchClass, Key, SPNPitch}
import idiocy.ui.clipboard._
import idiocy.ui.data.TimeSig
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
    * Merge rest events vertically.
    *
    * If there are two rests on the same staff, then replace them with a single rest.
    * If there are two rest events on both staffs, replace them with a single full rest.
    * Remove central partial rests
    * If there are partial rests on the same line as un-spanned notes, remove them
    *
    * @param eventSet the event set to fix.
    * @return
    */
  def fixUpRests(eventSet: MusicEventSet): MusicEventSet = {
    var upperFullRests = Array[Int]()
    var lowerFullRests = Array[Int]()
    var upperPartialRests = Array[Int]()
    var upperSpans = Array[Int]()
    var upperNotes = Array[MusicNote]()
    var lowerPartialRests = Array[Int]()
    var lowerSpans = Array[Int]()
    var lowerNotes = Array[MusicNote]()
    var centralNotes = Array[MusicNote]()
    var centralPartialRests = Array[Int]()
    var centralSpans = Array[Int]()

    var i = 0
    while(i < eventSet.events.length) {
      val event = eventSet.events(i)
      event match {
        case note: MusicNote =>
          if (note.barLocation > 0)
            upperNotes = upperNotes :+ note
          else if (note.barLocation < 0)
            lowerNotes = lowerNotes :+ note
          else
            centralNotes = centralNotes :+ note

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

    //I actually can't think of a time when we want central partial rests
    if(centralPartialRests.nonEmpty){
      newTheseEvents = newTheseEvents.removeIndexes(centralPartialRests)
    }

    //we don't want partial rests if there are only notes that are non-spanned
    if(upperNotes.nonEmpty && upperPartialRests.nonEmpty && upperSpans.isEmpty){
      val keep = upperNotes.exists(i=>i.lengthPips != eventSet.lengthPips)
      if(!keep)
        newTheseEvents = newTheseEvents.removeIndexes(upperPartialRests)
    }

    if(lowerNotes.nonEmpty && lowerPartialRests.nonEmpty && lowerSpans.isEmpty){
      val keep = lowerNotes.exists(i=>i.lengthPips != eventSet.lengthPips)
      if(!keep)
        newTheseEvents = newTheseEvents.removeIndexes(lowerPartialRests)
    }

    //checks when there are no notes
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


  def fixUp(events: Array[MusicEventSet], smoosh: Boolean = true) :Array[MusicEventSet] = {
    //if there are rests missing in, add them in
    var out = events.map(e => GrandStaff.fixUpRests(e))

    //now smoosh
    if(smoosh)
      out = MusicEventSet.smoosh(out)

    MusicEventSet.splitAtMeasureBoundaries(out)
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

  def findEventIndexes(events: Array[MusicEvent], atBarLine: Int): Array[Int] = {
    var j = 0
    var out = Array[Int]()
    while(j < events.length){
      events(j) match{
        case note: MusicNote =>
          if(note.barLocation == atBarLine)
            out = out :+ j
        case fullRest: FullRest =>
          if((fullRest.barLocation > 0 && atBarLine >= 0) || (fullRest.barLocation < 0 && atBarLine <= 0))
            out = out :+ j
        case rest: PartialRest =>
          if(rest.barLocation == atBarLine)
            out = out :+ j
        case span: MusicNoteSpan =>
          if(span.barLocation == atBarLine)
            out = out :+ j
        case _ =>
      }
      j += 1
    }
    out
  }

  def stripOutBarLine(events: Array[MusicEventSet],
                      from: Int,
                      barLocation: Int,
                      lengthPips: Int): Array[MusicEventSet] = {
    val unchangedBeginStub = ArrayUtils.subRange(events, 0, from)
    var out = unchangedBeginStub
    var remainingLength = lengthPips
    var i = from
    var firstIteration = true
    while(i < events.length && remainingLength > 0){
      var theseEvents = events(i)
      val theseEventsLength = theseEvents.lengthPips
      var eventIndexes = GrandStaff.findEventIndexes(theseEvents.events, barLocation)
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
        eventIndexes = GrandStaff.findEventIndexes(theseEvents.events, barLocation)
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

  def overwriteRest(events: Array[MusicEventSet],
                    lengthPips: Int,
                    eventId: Int, barLine: Int,
                    smoosh: Boolean = false): Array[MusicEventSet] = {
    val newEvents: Array[MusicEventSet] = if(eventId >= events.length){
      events :+ new MusicEventSet(Array[MusicEvent](
        FullRest(lengthPips, GrandStaff.UpperRestBarLine),
        FullRest(lengthPips, GrandStaff.LowerRestBarLine)),
        lengthPips)
    } else {

      var newEvents = stripOutBarLine(events, eventId, barLine, lengthPips)

      //now put in the rest, possibly splitting
      newEvents = MusicEventSet.insertPartialRest(newEvents, eventId, lengthPips, barLine)

      //fix up any problems. By not smooshing we let rests get split up
      newEvents = GrandStaff.fixUp(newEvents, smoosh)

      newEvents
    }

    newEvents
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

  private [this] def getClipboardEventSets(eventSets: Array[MusicEventSet],
                                           from: Int,
                                           to: Int,
                                           initKey: Key): (ArrayBuffer[ClipboardEventSet], Key) = {
    val out = ArrayBuffer[ClipboardEventSet]()
    var eventId = from
    var key = initKey
    while(eventId < to){
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
    (out, key)
  }

  override def getClipboardEventsFromSelection(selection: EventSelection): Array[ClipboardEventSet] = {
    var eventIdL = getStaffEventIdFromMeasureEventId(selection.meidL)
    val eventIdR = getStaffEventIdFromMeasureEventId(selection.meidR)
    val key = getKey(eventIdL)
    val (out, _) = getClipboardEventSets(events, eventIdL, eventIdR, key)

    out.toArray
  }

  def getClipboardEventsFromSelection(selection: MeasureSelection): Array[ClipboardEventSet] = {
    val out = ArrayBuffer[ClipboardEventSet]()
    var measureId = selection.measureIdL
    var initEventId = getStaffEventIdFromMeasureEventId(MeasureEventId(selection.measureIdL, 0))
    var key: Key = getKey(initEventId)
    while(measureId <= selection.measureIdR && measureId < measures.length - 1){
      val measure = measures(measureId)
      val (thisOut, k) = getClipboardEventSets(measure.events, 0, measure.events.length - 1, key)
      key = k
      out ++= thisOut
      measureId += 1
    }
    out.toArray
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

  def getMeasureUnsafe(measureId: Int): Measure = {
    measures(measureId)
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

  /**
    * This is an overwrite implementation. It essentially takes notes and replaces them with rests, ignores rests, and
    * deletes all other event types
    *
    * @param cursor the cursor
    * @return
    */
  override def backspaceAtCursor(cursor: Cursor): (Staff, Cursor) = {
    val cursorEventId = getStaffEventIdFromMeasureEventId(cursor.measureEventId) - 1
    if(cursorEventId >= 0) {
      val eventSet = events(cursorEventId)
      val toRemove = ArrayBuffer[Int]()
      val toReplace = ArrayBuffer[(Int, Int, Int)]()
      val eventIndexes = GrandStaff.findEventIndexes(eventSet.events, cursor.barLine)
      var i = 0
      while(i < eventIndexes.length) {
        val event = eventSet(eventIndexes(i))
        event match {
          case note: MusicNote =>
            toReplace += ((note.lengthPips, cursorEventId, cursor.barLine))

          case span: MusicNoteSpan =>
            //step back, find source, strip out bar
            var j = cursorEventId - 1
            var looking = true
            while(j >= 0){
              val es = events(j)
              val ei = GrandStaff.findEventIndexes(es.events, cursor.barLine)
              val oRootNoteIdx = ei.find(eii=>es(eii) match {
                case note: MusicNote => true
                case _ => false
              })
              oRootNoteIdx.foreach(rootNoteIdx => {
                val rootNote = es(rootNoteIdx)
                toReplace += ((rootNote.lengthPips, j, cursor.barLine))
                looking = false
              })
              j -= 1
            }

          case _: MusicRest => //don't do anything

          case _ =>
            //this will eventually be things like time sigs
            toRemove += cursor.barLine
        }
        i += 1
      }

      var outEvents = ArrayUtils.replaceElem(events, cursorEventId, eventSet.removeIndexes(toRemove.toArray))
      i = 0
      while(i < toReplace.length){
        outEvents = GrandStaff.overwriteRest(outEvents, toReplace(i)._1, toReplace(i)._2, toReplace(i)._3, smoosh = true)
        i += 1
      }

      outEvents = GrandStaff.fixUp(outEvents)

      val staffOut = new GrandStaff(outEvents, GrandStaff.generateMeasures(outEvents))
      val oCursorOut = staffOut.cursorMove(cursor, Cursor.LEFT)
      (staffOut, oCursorOut.get)
    } else {
      (this, cursor)
    }
  }

  override def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (Staff, Cursor) = {
    //this is all overwrite mode
    val cursorEventId = getStaffEventIdFromMeasureEventId(cursor.measureEventId)
    val newEvents = GrandStaff.overwriteRest(events, lengthPips, cursorEventId, cursor.barLine)
    val newGS = new GrandStaff(newEvents, GrandStaff.generateMeasures(newEvents))
    val oCursorOut = newGS.cursorMove(cursor, Cursor.RIGHT)
    (newGS, oCursorOut.get)
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
        var newEvents = GrandStaff.stripOutBarLine(events, cursorEventId, cursor.barLine, lengthPips)

        //now put in the note, possibly splitting
        newEvents = MusicEventSet.insertNote(newEvents, cursorEventId,
          new MusicNote(lengthPips, cursor.barLine, noteIdModifier))

        //fix up any problems
        newEvents = GrandStaff.fixUp(newEvents)

        newEvents
      }
    val newGS = new GrandStaff(newEvents, GrandStaff.generateMeasures(newEvents))
    val oCursorOut = newGS.cursorMove(cursor, Cursor.RIGHT)
    (newGS, oCursorOut.get)
  }

  override def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (Staff, Cursor) = {
    val cursorEventId = getStaffEventIdFromMeasureEventId(cursor.measureEventId)

    val timeSig = getTimeSig(cursorEventId)
    var insertLengthPips = timeSig.measureLengthPips * numMeasures

    //0 - find out how far I am into the measure
    val measure = measures(cursor.measureId)
    var i = 0
    var measureBreakPips = 0
    while(i < cursor.measureEventId.eventId){
      val eventSet = measure.events(i)
      measureBreakPips += eventSet.lengthPips
      i += 1
    }

    //1 - split events into the set before, the set of spans in the middle (with partial rests stripped out),
    // the set of notes and full rests after (with spans stripped out)
    val before = ArrayBuffer[MusicEventSet]()
    val pivot = ArrayBuffer[MusicEventSet]()
    val after = ArrayBuffer[MusicEventSet]()
    i = 0
    while(i < cursorEventId){
      before += events(i)
      i += 1
    }

    val barLinesWithSpans = mutable.Set[Int]()
    var j = 0
    val eventSet = events(i)
    while(j < eventSet.length){
      val event = eventSet(j)
      event match{
        case span: MusicNoteSpan => barLinesWithSpans += span.barLocation
        case _ =>
      }
      j += 1
    }

    while(i < events.length){
      val eventSet = events(i)
      var j = 0
      val pivotEventSet = ArrayBuffer[MusicEvent]()
      val afterEventSet = ArrayBuffer[MusicEvent]()
      val newBarLinesWithSpans = mutable.Set[Int]()
      while(j < eventSet.length){
        val event = eventSet(j)
        event match{
          case span: MusicNoteSpan => if(barLinesWithSpans.contains(span.barLocation)){
            newBarLinesWithSpans += span.barLocation
            pivotEventSet += span
          }
          else
            afterEventSet += span
          case _ => afterEventSet += event
        }
        j += 1
      }

      if(pivotEventSet.nonEmpty)
        pivot += new MusicEventSet(pivotEventSet.toArray, eventSet.lengthPips)
      if(afterEventSet.nonEmpty)
        after += new MusicEventSet(afterEventSet.toArray, eventSet.lengthPips)

      barLinesWithSpans.clear()
      barLinesWithSpans ++= newBarLinesWithSpans

      i += 1
    }

    //2 - insert rests to the length of lengthPips, less the size of the pivot, and breaking at measure end
    val pivotLengthPips = pivot.foldLeft(0)((z,e)=>z+e.lengthPips)
    insertLengthPips -= pivotLengthPips
    val out = ArrayBuffer[MusicEventSet]()
    out ++= before
    out ++= pivot

    val pad = ArrayBuffer[MusicEventSet]()
    pad += new MusicEventSet(Array(new FullRest(insertLengthPips, GrandStaff.UpperRestBarLine),
      new FullRest(insertLengthPips, GrandStaff.LowerRestBarLine)), insertLengthPips)

    out ++= pad
    out ++= after

    val fixedUp = GrandStaff.fixUp(out.toArray)

    (new GrandStaff(fixedUp, GrandStaff.generateMeasures(fixedUp)), cursor)
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

  override def cursorMoveVerticalWithinStaff(cursor: Cursor, direction: Int): Option[Cursor] = direction match {
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
