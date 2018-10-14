package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

class SimpleMonoGain(e: Engine,
                     val in: SignalBufferIn,
                     val gain: SignalBufferIn) extends UGen(e) with UGenSignalOut1 {
  initOut(e, in.sampleRate)
  private [this] val inReaderId: in.ReaderId = in.linkTo(in.sampleRate)
  private [this] val gainReaderId: gain.ReaderId = gain.linkTo(in.sampleRate)

  private [this] def getHowMuch: Int = {
    math.min(bufferOut.writeCapacity, math.min(in.readCapacity(inReaderId), gain.readCapacity(gainReaderId)))
  }


  override def runInternal(): Int = {
    val howMuch = getHowMuch
    var i = 0
    var lw = bufferOut.writePtr
    var inReadPtr = in.readPtr(inReaderId)
    var gainReadPtr = gain.readPtr(gainReaderId)
    while (i < howMuch) {

      bufferOut.b(lw) = in.b(inReadPtr) * gain.b(gainReadPtr)
      inReadPtr = in.nextIndex(inReadPtr)
      gainReadPtr = gain.nextIndex(gainReadPtr)
      lw = bufferOut.nextIndex(lw)
      i += 1
    }

    bufferOut.commitWrite(howMuch)
    in.commitRead(howMuch, inReaderId)
    gain.commitRead(howMuch, gainReaderId)
    howMuch
  }
}
