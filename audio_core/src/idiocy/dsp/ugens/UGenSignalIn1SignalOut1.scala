package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

abstract class UGenSignalIn1SignalOut1(e: Engine, val in: SignalBufferIn, sampleRate: Int)
  extends UGen(e) with UGenSignalOut1 {
  val bIndex: Int = in.linkTo(sampleRate)
  initOut(e, sampleRate)

  def update(in: Float): Float

  override def runInternal(): Int = {
    val howMuch = math.min(bufferOut.writeCapacity, in.readCapacity(bIndex))

    var i = 0
    var lw = bufferOut.writePtr
    var lr = in.readPtr(bIndex)
    while(i < howMuch) {
      bufferOut.b(lw) = update(in.b(lr))
      lw = bufferOut.nextIndex(lw)
      lr = in.nextIndex(lr)
      i += 1
    }
    bufferOut.commitWrite(howMuch)
    in.commitRead(howMuch, bIndex)
    howMuch
  }
}
