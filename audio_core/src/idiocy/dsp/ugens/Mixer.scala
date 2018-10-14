package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}

/**
  * Mixes a set of inputs
  * @param e the engine
  * @param in array of inputs
  * @param gains array of gains - either zero (in which case the mixer is dumb as rocks and simply sums) or controlled
  *              by an array of gains that is the same length at the array of inputs
  */
class Mixer(e: Engine,
            val in: Array[SignalBufferIn],
            val gains: Array[SignalBufferIn] = Array()) extends UGen(e) with UGenSignalOut1 {
  private [this] val num: Int = in.length
  private [this] val inReaderIds: Array[Int] = new Array(num)
  private [this] val gainReaderIds: Array[Int] = new Array(gains.length)
  private [this] val hasGains = gains.length != 0

  {
    if(gains.length != 0 && gains.length != in.length)
      throw new IllegalArgumentException("gains must either be empty or one per input")

    var i = 0
    while (i < num) {
      if(i != 0) {
        if(in(i).sampleRate != in(i-1).sampleRate)
          throw new IllegalArgumentException("sample rates must be the same")
      }

      inReaderIds(i) = in(i).linkTo(in(i).sampleRate)
      if(hasGains)
        gainReaderIds(i) = gains(i).linkTo(gains(i).sampleRate)

      i += 1
    }

    initOut(e, in(0).sampleRate)
  }

  private [this] def getHowMuch: Int = {
    var howMuch = bufferOut.writeCapacity
    var i = 0
    while(i < num) {
      if(hasGains)
        howMuch = math.min(howMuch,
          math.min(in(i).readCapacity(inReaderIds(i)),
            gains(i).readCapacity(gainReaderIds(i))))
      else
        howMuch = math.min(howMuch,in(i).readCapacity(inReaderIds(i)))
      i += 1
    }
    howMuch
  }

  //irritating buffers so I don't need to realloc every cycle
  private [this] val inReadPtrs = new Array[Int](num)
  private [this] val gainReadPtrs = new Array[Int](gains.length)

  override def runInternal(): Int = {
    val howMuch = getHowMuch

    var i = 0
    var writePtr = bufferOut.writePtr
    var j = 0
    while(j < num){
      inReadPtrs(j) = in(j).readPtr(inReaderIds(j))
      if(hasGains)
        gainReadPtrs(j) = gains(j).readPtr(gainReaderIds(j))
      j += 1
    }

    while(i < howMuch) {
      var j = 0
      bufferOut.b(writePtr) = 0
      while(j < in.length){
        if(hasGains)
          bufferOut.b(writePtr) += in(j).b(inReadPtrs(j)) * gains(j).b(gainReadPtrs(j))
        else
          bufferOut.b(writePtr) += in(j).b(inReadPtrs(j))
        inReadPtrs(j) = in(j).nextIndex(inReadPtrs(j))
        if(hasGains)
          gainReadPtrs(j) = gains(j).nextIndex(gainReadPtrs(j))
        j += 1
      }

      writePtr = bufferOut.nextIndex(writePtr)
      i += 1
    }

    bufferOut.commitWrite(howMuch)
    j = 0
    while(j < num){
      in(j).commitRead(howMuch, inReaderIds(j))
      if(hasGains)
        gains(j).commitRead(howMuch, gainReadPtrs(j))
      j += 1
    }
    howMuch
  }
}
