package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

abstract class UGenSignalIn1SignalOut0(e: Engine, val in: SignalBufferIn, sampleRate: Int) extends UGen(e) {
  val bi: Int = in.linkTo(sampleRate)

  override def runInternal(): Int = {
    val howMuch = in.readCapacity(bi)

    var i = 0

    var lr = in.readPtr(bi)
    while(i < howMuch) {
      update(in.b(lr))

      lr = in.nextIndex(lr)
      i += 1
    }
    in.commitRead(howMuch, bi)
    howMuch
  }

  def update(in: Float): Unit
}
