package idiocy.dsp.ugens

import idiocy.dsp.core.Engine

abstract class UGenSignalIn0SignalOut1(e: Engine, sampleRate: Int)
  extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate)
  def runInternal(): Int = {
    val howMuch = bufferOut.writeCapacity

    var i = 0
    var l = bufferOut.writePtr
    while(i < howMuch) {
      out.b(l) = update()
      l = out.nextIndex(l)
      i += 1
    }
    bufferOut.commitWrite(howMuch)
    howMuch
  }

  def update(): Float
}
