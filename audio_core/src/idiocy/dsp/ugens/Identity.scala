package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

class Identity(e: Engine, in: SignalBufferIn) extends UGenSignalIn1SignalOut1(e, in, in.sampleRate){
  override def update(in: Float): Float = in
}
