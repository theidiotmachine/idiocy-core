package idiocy.dsp.ugens

import idiocy.dsp.core._
import idiocy.dsp.interpolation.{CubicSplineInterpolation, InterpolationAlgorithm}
import idiocy.dsp.sample.{StereoSample}


/**
  * Monophonic sample player that can do speed and volume manipulation.
  * @param e the engine
  * @param gain gain control
  * @param speed speed control. multiple of original speed.
  * @param trigger control channel. -2 == stop. 0..number of samples, play that sample. -1, carry on
  * @param samples array of samples
  * @param interpolationAlgorithm interpolation algorithm for playing at non unit speed. Use SampleAndHold if you
  *                               are not going to pitch bend.
  */
class MonophonicStereoSamplePlayer(e: Engine,
                                   val gain: SignalBufferIn,
                                   val speed: SignalBufferIn,
                                   val trigger: SignalBufferIn,
                                   val synch: SignalBufferIn,
                                   val samples: Array[StereoSample],
                                   val interpolationAlgorithm: InterpolationAlgorithm = new CubicSplineInterpolation)
  extends UGen(e) with UGenSignalOut2{

  {
    var j = 1
    while(j < samples.length) {
      if(samples(j)==null)
        ???
      if(samples(j-1).sampleRate != samples(j).sampleRate)
        throw new IllegalArgumentException("All samples must have the same sample rate")
      j += 1
    }
  }

  initOut(e, samples(0).sampleRate.toInt)

  private [this] val speedReaderId = speed.linkTo(samples(0).sampleRate)
  private [this] val gainReaderId = gain.linkTo(samples(0).sampleRate)
  private [this] val triggerReaderId = trigger.linkTo(samples(0).sampleRate)
  private [this] val synchReaderId = synch.linkTo(samples(0).sampleRate)
  private [this] var sampleLoc: Double = 0
  private [this] var playingSample: Int = -1
  private [this] var sampleNumber: Long = 0

  override def runInternal(): Int = {
    val howMuch = calcHowMuch

    var i = 0
    var oll = lBufferOut.writePtr
    var orl = rBufferOut.writePtr

    var gainReadPtr = gain.readPtr(gainReaderId)
    var speedReadPtr = speed.readPtr(speedReaderId)
    var triggerReadPtr = trigger.readPtr(triggerReaderId)

    while(i < howMuch) {
      val t = trigger.b(triggerReadPtr)
      if(t == -2) {
        playingSample = -1
        sampleLoc = 0
      } else if (t >= 0) {
        playingSample = t.toInt
        sampleLoc = 0
      }

      //so if we have a sample playing, copy it out
      if(playingSample >= 0) {
        val sample = samples(playingSample)
        lBufferOut.b(oll) = gain.b(gainReadPtr) * sample.interpolate(0, sampleLoc, interpolationAlgorithm)
        rBufferOut.b(orl) = gain.b(gainReadPtr) * sample.interpolate(1, sampleLoc, interpolationAlgorithm)
        sampleLoc += speed.b(speedReadPtr)
        if(sampleLoc >= sample.lBuffer.length) {
          sampleLoc = 0
          playingSample = -1
        }
      } else {
        lBufferOut.b(oll) = 0
        rBufferOut.b(orl) = 0
      }

      oll = lBufferOut.nextIndex(oll)
      orl = rBufferOut.nextIndex(orl)
      gainReadPtr = gain.nextIndex(gainReadPtr)
      speedReadPtr = speed.nextIndex(speedReadPtr)
      triggerReadPtr = trigger.nextIndex(triggerReadPtr)
      i += 1
      sampleNumber += 1
    }

    lBufferOut.commitWrite(howMuch)
    rBufferOut.commitWrite(howMuch)
    gain.commitRead(howMuch, gainReaderId)
    speed.commitRead(howMuch, speedReaderId)
    synch.commitRead(howMuch, synchReaderId)
    trigger.commitRead(howMuch, triggerReaderId)
    howMuch
  }

  private [this] def calcHowMuch: Int = {
    math.min(lBufferOut.writeCapacity,
      math.min(rBufferOut.writeCapacity,
        math.min(gain.readCapacity(gainReaderId),
          math.min(speed.readCapacity(speedReaderId),
            math.min(synch.readCapacity(synchReaderId),
              trigger.readCapacity(triggerReaderId)
            )))))
  }
}
