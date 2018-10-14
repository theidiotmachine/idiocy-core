package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.filter.Filter

class SimpleMonoDelay(e: Engine,
                      val delayTime: Float,
                      val wetGain: Float,
                      val filter: Filter,
                      val in: SignalBufferIn,
                      val dryGain: Float = 1f) extends UGen(e) with UGenSignalOut1 {
  initOut(e, in.sampleRate)
  filter.calcCoeff()
  private [this] val delaySz = (delayTime * in.sampleRate).toInt
  private [this] val delayBuffer: Array[Float] = new Array(delaySz)
  private [this] var delayLoc = 0

  private [this] val inReaderId: in.ReaderId = in.linkTo(in.sampleRate)

  private [this] def getHowMuch: Int = {
    math.min(bufferOut.writeCapacity, in.readCapacity(inReaderId))
  }

  override def runInternal(): Int = {
    val howMuch = getHowMuch
    var i = 0
    var lw = bufferOut.writePtr
    var lr = in.readPtr(inReaderId)
    while (i < howMuch) {

      bufferOut.b(lw) = in.b(lr) * dryGain + filter(delayBuffer(delayLoc)) * wetGain
      delayBuffer(delayLoc) = bufferOut.b(lw)

      delayLoc += 1
      if(delayLoc == delaySz)
        delayLoc = 0
      lr = in.nextIndex(lr)
      lw = bufferOut.nextIndex(lw)
      i += 1
    }

    bufferOut.commitWrite(howMuch)
    in.commitRead(howMuch, inReaderId)
    howMuch
  }
}
