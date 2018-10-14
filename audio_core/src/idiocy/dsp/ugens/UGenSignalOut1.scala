package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBuffer, SignalBufferIn, SignalBufferOut}

trait UGenSignalOut1{
  private [this] var b: SignalBuffer = _
  protected [this] var bufferOut: SignalBufferOut = _
  var out: SignalBufferIn = _
  private [this] var _signalOuts: Array[SignalBufferIn] = _

  def signalOuts: Array[SignalBufferIn] = _signalOuts

  def initOut(e: Engine, sampleRate: Int): Unit = {
    b = new SignalBuffer(e, sampleRate)
    bufferOut = b
    out = b
    _signalOuts = Array(out)
  }
}
