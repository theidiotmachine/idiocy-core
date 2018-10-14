package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

class StereoMaxCombiner(e: Engine, l: SignalBufferIn, r: SignalBufferIn) extends UGen(e) with UGenSignalOut1 {
  initOut(e, l.sampleRate)
  val lReaderId: Int = l.linkTo(r.sampleRate)
  val rReaderId: Int = r.linkTo(l.sampleRate)

  override def runInternal(): Int = {
    val howMuch = math.min(bufferOut.writeCapacity, math.min(l.readCapacity(lReaderId), r.readCapacity(rReaderId)))

    var i = 0
    var lw = bufferOut.writePtr

    var lReadPtr = l.readPtr(lReaderId)
    var rReadPtr = r.readPtr(rReaderId)
    while(i < howMuch) {
      bufferOut.b(lw) = math.max(math.abs(l.b(lReadPtr)), math.abs(r.b(rReadPtr)))

      lw = bufferOut.nextIndex(lw)
      lReadPtr = l.nextIndex(lReadPtr)
      rReadPtr = r.nextIndex(rReadPtr)
      i += 1
    }
    bufferOut.commitWrite(howMuch)
    l.commitRead(howMuch, lReaderId)
    r.commitRead(howMuch, rReaderId)
    howMuch
  }
}
