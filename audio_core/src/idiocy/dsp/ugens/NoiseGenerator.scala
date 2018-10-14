package idiocy.dsp.ugens

import idiocy.dsp.core.Engine

class NoiseGenerator(e: Engine, sampleRate: Int) extends UGenSignalIn0SignalOut1(e, sampleRate){
  override def update(): Float = (math.random().toFloat * 2.0f) - 1.0f
}
