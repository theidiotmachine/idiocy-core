package idiocy.dsp.ugens

import idiocy.dsp.core.Engine
import idiocy.dsp.oscillator.SineOscillator

class SineUGen(e: Engine,
               val gain: Float,
               val frequency: Float,
               sampleRate: Int,
               val maths: Float => Float = in => in,
               phase: Float = 0) extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate.toInt)

  private [this] val osc = new SineOscillator(phase)

  override def runInternal(): Int = {
    val howMuch = bufferOut.writeCapacity

    var i = 0
    var l = bufferOut.writePtr
    //val twoPiF = 2.0 * Math.PI * frequency.toDouble
    val dp: Double = frequency.toDouble / sampleRate.toDouble
    while(i < howMuch) {
      /*
      val time: Double = i.toDouble / sampleRate.toDouble
      bufferOut.b(l) = gain * maths(Math.sin(twoPiF * time + phase).toFloat)
      */
      bufferOut.b(l) = gain * maths(osc.apply(dp))
      l = bufferOut.nextIndex(l)
      i += 1
    }
    //phase = twoPiF * howMuch / sampleRate + phase
    bufferOut.commitWrite(howMuch)
    howMuch
  }
}
