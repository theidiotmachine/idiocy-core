package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn, SignalBufferOut}

abstract class UGen(e:  Engine) {
  e.register(this)
  def run(): Int = synchronized { runInternal() }
  def runInternal(): Int
  def signalOuts: Array[SignalBufferIn]
}
