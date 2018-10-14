package idiocy.ui.renderer

import idiocy.ui.utils.ArrayUtils

object DisplayEventSet{
  def extendEventSet(theseEvents: DisplayEventSet, l: Long): DisplayEventSet = {
    new DisplayEventSet(theseEvents.events.map {
      case note: DisplayNote => new DisplayNote(l, note.barLocation, note.accidental)
      case rest: DisplayPartialRest => DisplayPartialRest(l, rest.barLocation)
      case rest: DisplayFullRest => DisplayFullRest(l, rest.barLocation)
      case span: DisplaySpan => DisplaySpan(l, span.barLocation, span.accidental)
    }, l)
  }
}

final class DisplayEventSet(val events: Array[DisplayEvent], val lengthPips: Long) {
  def :+ (displayEvent: DisplayEvent): DisplayEventSet = {
    new DisplayEventSet(events :+ displayEvent, lengthPips)
  }

  def apply(idx: Int): DisplayEvent = events(idx)
  def length: Int = events.length

  def removeIndex(idx: Int): DisplayEventSet = {
    new DisplayEventSet(ArrayUtils.removeIndex(events, idx), lengthPips)
  }

  def removeIndexes(idxes: Array[Int]): DisplayEventSet = {
    new DisplayEventSet(ArrayUtils.removeIndexes(events, idxes), lengthPips)
  }

  def findEventIndexes(atBarLine: Int): Array[Int] = {
    var j = 0
    var out = Array[Int]()
    while(j < events.length){
      events(j) match{
        case note: DisplayNote =>
          if(note.barLocation == atBarLine)
            out = out :+ j
        case fullRest: DisplayFullRest =>
          if((fullRest.barLocation > 0 && atBarLine >= 0) || (fullRest.barLocation < 0 && atBarLine <= 0))
            out = out :+ j
        case rest: DisplayPartialRest =>
          if(rest.barLocation == atBarLine)
            out = out :+ j
        case span: DisplaySpan =>
          if(span.barLocation == atBarLine)
            out = out :+ j
        case _ =>
      }
      j += 1
    }
    out
  }
}
