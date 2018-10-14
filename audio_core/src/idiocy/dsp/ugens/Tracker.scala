package idiocy.dsp.ugens

import idiocy.dsp.core._

class Tracker(e: Engine, sampleRate: Int, trackerData: Array[Event]) extends UGen(e) with UGenSignalOut0{
  private [this] val b = new EventBuffer(sampleRate)
  private [this] val bOut: EventBufferOut = b
  val out: EventBufferIn = b
  var ptr = 0
  override def runInternal(): Int = {
    val howMany = bOut.writeCapacity
    var ew = bOut.writePtr
    var numEv = 0

    while(numEv < howMany && ptr < trackerData.length) {
      val ev = bOut.b(ew)
      ev.copyFrom(trackerData(ptr))

      ew = bOut.nextIndex(ew)
      ptr += 1
      numEv += 1
    }

    if(numEv < howMany){
      val i = 3
    }

    bOut.commitWrite(numEv, true)
    0 //???
  }
}

