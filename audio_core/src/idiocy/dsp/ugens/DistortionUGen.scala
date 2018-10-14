package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.distortion.Distortion

class DistortionUGen(e: Engine, in: SignalBufferIn, distortion: Distortion) extends UGenSignalIn1SignalOut1(e, in, in.sampleRate){
  override def update(in: Float): Float = distortion.apply(in)
}
