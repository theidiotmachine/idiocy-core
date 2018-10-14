package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBuffer, SignalBufferIn, SignalBufferOut}

trait UGenSignalOut2 {
  private [this] var lb: SignalBuffer = _
  private [this] var rb: SignalBuffer = _
  protected [this] var lBufferOut: SignalBufferOut = _
  protected [this] var rBufferOut: SignalBufferOut = _
  var leftOut: SignalBufferIn = _
  var rightOut: SignalBufferIn = _
  private [this] var _signalOuts: Array[SignalBufferIn] = _
  def signalOuts: Array[SignalBufferIn] = _signalOuts

  def initOut(e: Engine, sampleRate: Int): Unit = {
    lb = new SignalBuffer(e, sampleRate)
    rb = new SignalBuffer(e, sampleRate)
    lBufferOut = lb
    rBufferOut = rb
    leftOut = lb
    rightOut = rb
    _signalOuts = Array(leftOut, rightOut)
  }
}
