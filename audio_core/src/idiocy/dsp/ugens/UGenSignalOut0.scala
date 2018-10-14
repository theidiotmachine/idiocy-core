package idiocy.dsp.ugens

import idiocy.dsp.core.SignalBufferIn

trait UGenSignalOut0 {
  private [this] var _signalOuts: Array[SignalBufferIn] = Array()
  def signalOuts: Array[SignalBufferIn] = _signalOuts
}
