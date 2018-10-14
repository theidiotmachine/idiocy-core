package idiocy.ui.clipboard

class ClipboardEventSet(val events: Array[ClipboardEvent], val lengthPips: Long) {
  def fixUpRests: ClipboardEventSet = {
    //the clipboard wants full rests that only fill an entire event set. The grand staff uses full rests
    //to fill its sub staffs
    val newEvents = events.filter {
      case _: ClipboardFullRest => false
      case _ => true
    }
    if(newEvents.isEmpty) {
      new ClipboardEventSet(Array(new ClipboardFullRest(lengthPips)), lengthPips)
    } else {
      new ClipboardEventSet(newEvents, lengthPips)
    }
  }
}
