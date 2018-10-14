package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, Event, EventBufferIn}


class EventPrinter(e: Engine, val events: EventBufferIn, sampleRate: Int) extends UGen(e) with UGenSignalOut0{
  private [this] val eventReaderId = events.linkTo(sampleRate)

  override def runInternal(): Int = {
    val howMany = events.readCapacity(eventReaderId)
    var numEv = 0
    var eventReadPtr = events.readPtr(eventReaderId)
    while(numEv < howMany) {
      val ev: Event = events.b(eventReadPtr)
      eventReadPtr = events.nextIndex(eventReadPtr)
      val sampleNumber = ev.sampleNumber

      ev.code match {
        case Event.EventCodePlayNote =>
          val noteId = ev.i0
          val velocity = ev.f0
          val volume = ev.f1

          println("Play Note ")

        case Event.EventCodeStopNote =>
          val release = ev.f0
      }

      numEv += 1
    }
    events.commitRead(numEv, eventReaderId)
    0 //???
  }
}
