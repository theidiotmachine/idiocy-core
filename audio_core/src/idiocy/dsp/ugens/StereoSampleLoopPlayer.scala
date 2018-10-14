package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.interpolation.{CubicSplineInterpolation, InterpolationAlgorithm}
import idiocy.dsp.sample.StereoSample

/**
  * A looping stereo sample player. Load your loops and thrash them!
  * @param gain Controls the gain
  * @param speed Controls how quickly the sample is played. 1 = original speed
  * @param sample The sample to play
  * @param interpolationAlgorithm Interpolation algorithm for speed changes.
  *                               Constant speed of 1 should make this SampleAndHold
  */
class StereoSampleLoopPlayer(e: Engine,
                             val gain: SignalBufferIn,
                             val speed: SignalBufferIn,
                             val sample: StereoSample,
                             val interpolationAlgorithm: InterpolationAlgorithm = new CubicSplineInterpolation)
  extends UGen(e) with UGenSignalOut2 {

  initOut(e, sample.sampleRate)
  private [this] val speedB = speed.linkTo(sample.sampleRate)
  private [this] val gainB = gain.linkTo(sample.sampleRate)
  private [this] var sampleLoc: Double = 0
  override def runInternal(): Int = {
    val howMuch = math.min(lBufferOut.writeCapacity,
      math.min(rBufferOut.writeCapacity,
        math.min(speed.readCapacity(speedB),
          gain.readCapacity(gainB))))

    var i = 0
    var oll = lBufferOut.writePtr
    var orl = rBufferOut.writePtr
    var igl = gain.readPtr(gainB)
    var isl = speed.readPtr(speedB)

    while(i < howMuch) {

      lBufferOut.b(oll) = gain.b(igl) * sample.interpolate(0, sampleLoc, interpolationAlgorithm)
      rBufferOut.b(orl) = gain.b(igl) * sample.interpolate(1, sampleLoc, interpolationAlgorithm)

      sampleLoc += speed.b(isl)
      if(sampleLoc >= sample.lBuffer.length)
        sampleLoc -= sample.lBuffer.length

      oll = lBufferOut.nextIndex(oll)
      orl = rBufferOut.nextIndex(orl)
      igl = gain.nextIndex(igl)
      isl = speed.nextIndex(isl)
      i += 1
    }

    lBufferOut.commitWrite(howMuch)
    rBufferOut.commitWrite(howMuch)
    gain.commitRead(howMuch, gainB)
    speed.commitRead(howMuch, speedB)

    howMuch
  }
}
