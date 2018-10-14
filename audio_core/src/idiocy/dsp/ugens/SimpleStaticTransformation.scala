package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

class SimpleStaticTransformation (e: Engine, in: SignalBufferIn, f: Float => Float) extends UGenSignalIn1SignalOut1(e, in, in.sampleRate){
  override def update(in: Float): Float = f(in)
}
