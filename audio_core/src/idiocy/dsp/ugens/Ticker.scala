package idiocy.dsp.ugens

import idiocy.dsp.core.Engine

/**
  * If you need to keep multiple processors in synch, create a single one of these and feed it into the synch channel
  * of all the processors
  * @param e          the engine
  * @param sampleRate the sample rate
  */
class Ticker(e: Engine, sampleRate: Int) extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate)
  override def runInternal(): Int = {
    var howMuch = calcHowMuch
    bufferOut.commitWrite(howMuch)
    howMuch
  }

  private [this] def calcHowMuch: Int = {
    bufferOut.writeCapacity
  }
}
