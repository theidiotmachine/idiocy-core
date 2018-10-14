package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

class NullOut(e: Engine, in: SignalBufferIn) extends UGenSignalIn1SignalOut0(e, in, in.sampleRate) with UGenSignalOut0{

  override def update(in: Float): Unit = {}
}
