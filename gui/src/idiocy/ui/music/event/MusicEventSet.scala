package idiocy.ui.music.event

import idiocy.music.key.Key
import idiocy.ui.data.TimeSig
import idiocy.ui.utils.ArrayUtils

import scala.collection.mutable.ArrayBuffer

object MusicEventSet{
  def insertNote(events: Array[MusicEventSet],
                 from: Int,
                 note: MusicNote): Array[MusicEventSet] = {
    val unchangedBeginStub = ArrayUtils.subRange(events, 0, from)
    var out = unchangedBeginStub
    var remainingLength = note.lengthPips
    var i = from

    var theseEvents = events(i)
    val theseEventsLength = theseEvents.lengthPips

    if(remainingLength < theseEventsLength){
      //the note is shorter than this event set. So split it the set, insert the new note, and stick a rest in straight
      // after
      val (new1, new2) = theseEvents.split(remainingLength)
      out = out :+
        (new1 :+ note) :+
        (new2 :+ PartialRest(theseEventsLength - remainingLength, note.barLocation))
      remainingLength = 0
      i += 1
    } else {
      //it's longer or the same, so add in the note
      theseEvents = theseEvents :+ note
      out = out :+ theseEvents
      remainingLength -= theseEventsLength
      i += 1

      //now keep adding spans in
      while (i < events.length && remainingLength > 0) {
        val theseEvents = events(i)
        val theseEventsLength = theseEvents.lengthPips
        if (theseEventsLength <= remainingLength) {
          remainingLength -= theseEventsLength
          out = out :+ (theseEvents :+ MusicNoteSpan(theseEventsLength, note.barLocation, note.accidental))
        } else {
          val (new1, new2) = theseEvents.split(remainingLength)
          remainingLength = 0
          out = out :+ (new1 :+ MusicNoteSpan(remainingLength, note.barLocation, note.accidental)) :+
            (new2 :+ PartialRest(theseEventsLength - remainingLength, note.barLocation))
        }

        i += 1
      }
    }

    if (i < events.length)
      out = out ++ ArrayUtils.subRange(events, i, events.length)
    else if(remainingLength > 0){
      out = out :+ new MusicEventSet(Array(MusicNoteSpan(remainingLength, note.barLocation, note.accidental)), remainingLength)
    }
    out
  }

  def insertPartialRest(events: Array[MusicEventSet], from: Int, lengthPips: Int, barLocation: Int): Array[MusicEventSet] = {
    val unchangedBeginStub = ArrayUtils.subRange(events, 0, from)
    var out = unchangedBeginStub
    var remainingLength = lengthPips
    var i = from

    val theseEvents = events(i)
    val theseEventsLength = theseEvents.lengthPips

    if(remainingLength < theseEventsLength){
      //split
      val (new1, new2) = theseEvents.split(remainingLength)
      out = out :+
        (new1 :+ PartialRest(remainingLength, barLocation)) :+ new2
      remainingLength = 0
      i += 1
    } else {
      while (i < events.length && remainingLength > 0) {
        val theseEvents = events(i)
        val theseEventsLength = theseEvents.lengthPips
        if (theseEventsLength <= remainingLength) {
          remainingLength -= theseEventsLength
          out = out :+ (theseEvents :+ PartialRest(theseEventsLength, barLocation))
        } else {
          val (new1, new2) = theseEvents.split(remainingLength)
          remainingLength = 0
          out = out :+ (new1 :+ PartialRest(remainingLength, barLocation)) :+ new2
        }

        i += 1
      }
    }

    if (i < events.length)
      out = out ++ ArrayUtils.subRange(events, i, events.length)
    else if(remainingLength > 0){
      out = out :+ new MusicEventSet(Array(PartialRest(remainingLength, barLocation)), remainingLength)
    }
    out
  }

  def smoosh(events: Array[MusicEventSet]): Array[MusicEventSet] = {
    var out = events
    var i = 0
    while(i < out.length - 1){
      var smooshCandidate = true
      val theseEvents = out(i)
      val nextEvents = out(i+1)
      val theseEventsArray = theseEvents.events
      var nextEventsArray = nextEvents.events
      var j = 0
      while(j < theseEvents.length && smooshCandidate){
        val event = theseEventsArray(j)
        val oei: Option[Int] = event match{
          case displayNote: MusicNote =>
            ArrayUtils.findIndex[MusicEvent](nextEventsArray, {
              case nextDisplaySpan: MusicNoteSpan=> nextDisplaySpan.barLocation == displayNote.barLocation
              case _ => false
            })

          case displaySpan: MusicNoteSpan =>
            ArrayUtils.findIndex[MusicEvent](nextEventsArray, {
              case nextDisplaySpan: MusicNoteSpan => nextDisplaySpan.barLocation == displaySpan.barLocation
              case _ => false
            })

          case displayRest: PartialRest =>
            ArrayUtils.findIndex[MusicEvent](nextEventsArray, {
              case nextDisplayRest: PartialRest => displayRest.barLocation == nextDisplayRest.barLocation
              case _ => false
            })

          case displayFullRest: FullRest =>
            ArrayUtils.findIndex[MusicEvent](nextEventsArray, {
              case nextDisplayFullRest: FullRest => displayFullRest.barLocation == nextDisplayFullRest.barLocation
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
        val smooshedEventSet = theseEvents.extendEventSet(theseEvents.lengthPips + nextEvents.lengthPips)
        out = ArrayUtils.replaceElem(out, i, smooshedEventSet)
        out = ArrayUtils.removeIndex(out, i+1)
      }
    }
    out
  }

  def splitAtMeasureBoundaries(events: Array[MusicEventSet]): Array[MusicEventSet] = {
    if(events.isEmpty)
      Array[MusicEventSet]()
    else{
      val out = ArrayBuffer[MusicEventSet]()
      var timeSig = TimeSig.timeSig44
      var timeLeftInMeasure = timeSig.measureLengthPips
      var eventSetIdx = 0
      while(eventSetIdx < events.length){
        val theseEvents = events(eventSetIdx)
        var eventIdx = 0

        var oNewTimeSig: Option[TimeSig] = None
        while(eventIdx < theseEvents.length){
          theseEvents(eventIdx) match {
            case timeSigEvent: TimeSigEvent =>
              oNewTimeSig = Some(timeSigEvent.timeSig)
            case _ =>
          }
          eventIdx += 1
        }

        oNewTimeSig.foreach(newTimeSig=>{
          timeLeftInMeasure = newTimeSig.measureLengthPips
          timeSig = newTimeSig
        })

        var toSplit = theseEvents
        while(toSplit.lengthPips > timeLeftInMeasure){
          val (l, r) = toSplit.split(timeLeftInMeasure)
          out += l
          timeLeftInMeasure = timeSig.measureLengthPips
          toSplit = r
        }
        out += toSplit
        timeLeftInMeasure -= toSplit.lengthPips
        if(timeLeftInMeasure == 0){
          timeLeftInMeasure = timeSig.measureLengthPips
        }

        eventSetIdx += 1
      }
      out.toArray
    }
  }
}

final class MusicEventSet(val events: Array[MusicEvent], val lengthPips: Int) {
  def getTimeSig: Option[TimeSig] = {
    events.foldLeft[Option[TimeSig]](None)(
      (b, e) => if(b.isDefined) b else e match {
        case timeSigEvent: TimeSigEvent => Some(timeSigEvent.timeSig)
        case _ => b
      })
  }

  def extendEventSet(l: Int): MusicEventSet = {
    new MusicEventSet(events.map {
      case note: MusicNote => new MusicNote(l, note.barLocation, note.accidental)
      case rest: PartialRest => PartialRest(l, rest.barLocation)
      case rest: FullRest => FullRest(l, rest.barLocation)
      case span: MusicNoteSpan => MusicNoteSpan(l, span.barLocation, span.accidental)
    }, l)
  }

  def apply(idx: Int): MusicEvent = events(idx)
  def length: Int = events.length
  def getKey: Option[Key] = {
    events.foldLeft[Option[Key]](None)(
      (b, e) => if(b.isDefined) b else e match {
        case keySignatureEvent: KeySignatureEvent => Some(keySignatureEvent.key)
        case _ => b
      })
  }

  def removeIndex(idx: Int): MusicEventSet = {
    new MusicEventSet(ArrayUtils.removeIndex(events, idx), lengthPips)
  }

  def removeIndexes(idxes: Array[Int]): MusicEventSet = {
    new MusicEventSet(ArrayUtils.removeIndexes(events, idxes), lengthPips)
  }

  def :+ (musicEvent: MusicEvent): MusicEventSet = {
    new MusicEventSet(events :+ musicEvent, lengthPips)
  }

  /**
    * Given where to split in pips, split this event set into two event sets, the first with the new
    * size and the second with the left-over size. Notes are split into spans, rests into sub-rests
    *
    * @param newLengthPips the new length in pips
    * @return
    */
  def split(newLengthPips: Int): (MusicEventSet, MusicEventSet) = {
    val stubLengthPips = lengthPips - newLengthPips
    var j = 0
    var outFirst = new MusicEventSet(Array(), newLengthPips)
    var outSecond = new MusicEventSet(Array(), stubLengthPips)
    while(j < events.length){
      val thisEvent = events(j)
      thisEvent match {
        case displayNote: MusicNote =>
          outFirst = outFirst :+ displayNote
          outSecond = outSecond :+ MusicNoteSpan(stubLengthPips, displayNote.barLocation, displayNote.accidental)
        case displayRest: PartialRest =>
          outFirst = outFirst :+ PartialRest(newLengthPips, displayRest.barLocation)
          outSecond = outSecond :+ PartialRest(stubLengthPips, displayRest.barLocation)
        case displayRest: FullRest =>
          outFirst = outFirst :+ FullRest(newLengthPips, displayRest.barLocation)
          outSecond = outSecond :+ FullRest(stubLengthPips, displayRest.barLocation)
        case displaySpan: MusicNoteSpan =>
          outFirst = outFirst :+ MusicNoteSpan(newLengthPips, displaySpan.barLocation, displaySpan.accidental)
          outSecond = outSecond :+ MusicNoteSpan(stubLengthPips, displaySpan.barLocation, displaySpan.accidental)
      }
      j += 1
    }
    (outFirst, outSecond)
  }
}
