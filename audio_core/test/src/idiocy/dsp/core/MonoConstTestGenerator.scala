package idiocy.dsp.core

import idiocy.dsp.ugens.{UGen, UGenSignalOut1}

class MonoConstTestGenerator(e: Engine,
                              sampleRate: Int,
                             generator: Int =>Float) extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate)
  var samples: Int = 0
  override def runInternal(): Int = {
    val howMuch = bufferOut.writeCapacity

    var i = 0
    var l = bufferOut.writePtr

    while(i < howMuch) {
      out.b(l) = generator(samples + i)
      l = out.nextIndex(l)
      i += 1
    }
    samples += howMuch

    bufferOut.commitWrite(howMuch)
    howMuch
  }

}
