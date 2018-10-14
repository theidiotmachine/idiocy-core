package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.filter.Filter

class FilterUGen(e: Engine,
                 val in: SignalBufferIn,
                 val freq: SignalBufferIn,
                 val arg2: SignalBufferIn,
                 val arg3: SignalBufferIn,
                 val filter: Filter) extends UGen(e) with UGenSignalOut1 {
  initOut(e, in.sampleRate)
  private [this] val inReaderId: in.ReaderId = in.linkTo(in.sampleRate)
  private [this] val freqReaderId: freq.ReaderId = freq.linkTo(freq.sampleRate)
  private [this] val arg2ReaderId: arg2.ReaderId = arg2.linkTo(arg2.sampleRate)
  private [this] val arg3ReaderId: arg3.ReaderId = arg3.linkTo(arg3.sampleRate)
  private [this] var currFreq = -1.0f
  private [this] var currArg2 = -1.0f
  private [this] var currArg3 = -1.0f
  private [this] var init = false

  private [this] def getHowMuch: Int = {
    math.min(bufferOut.writeCapacity,
      math.min(freq.readCapacity(freqReaderId),
        math.min(in.readCapacity(inReaderId),
          math.min(arg3.readCapacity(arg3ReaderId),
            arg2.readCapacity(arg2ReaderId)))))
  }

  override def runInternal(): Int = {
    val howMuch = getHowMuch
    var i = 0
    var lw = bufferOut.writePtr
    var inReadPtr = in.readPtr(inReaderId)
    var freqReadPtr = freq.readPtr(freqReaderId)
    var arg2ReadPtr = arg2.readPtr(arg2ReaderId)
    var arg3ReadPtr = arg3.readPtr(arg3ReaderId)

    while (i < howMuch) {
      val newFreq = freq.b(freqReadPtr)
      val newArg2 = arg2.b(arg2ReadPtr)
      val newArg3 = arg3.b(arg3ReadPtr)
      if(!init || currFreq != newFreq || currArg2 != newArg2 || currArg3 != newArg3) {
        filter.freq = newFreq
        filter.setArg2(newArg2)
        filter.setArg3(newArg3)
        filter.calcCoeff()
        init = true
        currFreq = newFreq
        currArg2 = newArg2
        currArg3 = newArg3
      }

      bufferOut.b(lw) = filter(in.b(inReadPtr))
      inReadPtr = in.nextIndex(inReadPtr)
      freqReadPtr = freq.nextIndex(freqReadPtr)
      arg2ReadPtr = arg2.nextIndex(arg2ReadPtr)
      arg3ReadPtr = arg3.nextIndex(arg3ReadPtr)
      lw = bufferOut.nextIndex(lw)
      i += 1
    }

    bufferOut.commitWrite(howMuch)
    in.commitRead(howMuch, inReaderId)
    freq.commitRead(howMuch, freqReaderId)
    arg2.commitRead(howMuch, arg2ReaderId)
    arg3.commitRead(howMuch, arg3ReaderId)
    howMuch
  }
}
