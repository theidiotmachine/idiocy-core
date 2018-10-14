package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.dynamicRange.TriggeredDynamicRange
import idiocy.dsp.envelope.Envelope
import idiocy.dsp.volume.VolumeDetector

/**
  * This allows for parallel compression. There is a fantastic article on Sound on Sound
  * (https://www.soundonsound.com/techniques/parallel-compression) that talks about it -
  * essentially you can layer a bunch of limiters or high ratio downward compressors with
  * the dry signal to get a kind of upward compressor. So we provide an array on all the things
  * you need to do this
  * @param e the engine
  * @param lIn left input
  * @param rIn right input
  * @param control control channel. Not sure? Use a StereoMaxCombiner on your inputs
  * @param volumeDetector your volume detector alg. Not sure? Use an RMS or a PeakDetector
  * @param dryGain gain to apply to the original signal. If you want it completely wet, it's zero
  * @param tdrs array of DynamicRange units. Not sure? Use a HardKneeDownwardCompressor
  * @param makeUpGains the gain applied to each unit above
  * @param envelopes the envelopes applied to each unit above. Not sure? Use EnvelopeSoftASR
  * @param lookAhead whether to feed the volume detector until it is full before using its results. Introduces
  *                  a delay equal to the window size of the detector
  */
class TriggeredDynamicRangeStereo(e: Engine,
                                  val lIn: SignalBufferIn,
                                  val rIn: SignalBufferIn,
                                  val control: SignalBufferIn,
                                  val volumeDetector: VolumeDetector,
                                  val dryGain: Float,
                                  val tdrs: Array[TriggeredDynamicRange],
                                  val makeUpGains: Array[Float],
                                  val envelopes: Array[Envelope],
                                  val lookAhead: Boolean
                       ) extends UGen(e) with UGenSignalOut2{
  initOut(e, lIn.sampleRate)


  private [this] val lInReaderId: lIn.ReaderId = lIn.linkTo(lIn.sampleRate)
  private [this] val rInReaderId: rIn.ReaderId = rIn.linkTo(rIn.sampleRate)
  private [this] val controlReaderId: control.ReaderId = control.linkTo(control.sampleRate)
  //private [this] var envelopeTime = 0.0f
  private [this] val sampleRate = lIn.sampleRate

  private [this] var delayAmount = if(lookAhead) volumeDetector.windowSz else 0

  private[this] def getHowMuch: Int = {
    math.min(lBufferOut.writeCapacity, math.min(rBufferOut.writeCapacity,
      math.min(lIn.readCapacity(lInReaderId), math.min(rIn.readCapacity(rInReaderId),
        control.readCapacity(controlReaderId)))))
  }

  override def runInternal(): Int = {
    if(delayAmount > 0) {
      val h0 = runLookAhead()
      val h1 = runFull()
      h0 + h1
    }
    else
      runFull()
  }

  private [this] def runLookAhead(): Int = {
    val howMuch = math.min(delayAmount, control.readCapacity(controlReaderId))
    var i = 0
    var controlReadPtr = control.readPtr(controlReaderId)
    while (i < howMuch) {
      //flush through
      volumeDetector.apply(control.b(controlReadPtr))
      controlReadPtr = control.nextIndex(controlReadPtr)
      i += 1
    }
    delayAmount -= howMuch
    control.commitRead(howMuch, controlReaderId)
    howMuch
  }

  private [this] def runFull(): Int = {
    val howMuch = getHowMuch
    var i = 0
    var llw = lBufferOut.writePtr
    var rlw = rBufferOut.writePtr
    var lInReadPtr = lIn.readPtr(lInReaderId)
    var rInReadPtr = rIn.readPtr(rInReaderId)
    var controlReadPtr = control.readPtr(controlReaderId)
    while (i < howMuch) {

      val volume = volumeDetector.apply(control.b(controlReadPtr))

      val l0 = lIn.b(lInReadPtr)
      val r0 = rIn.b(rInReadPtr)
      var lOut = dryGain * l0
      var rOut = dryGain * r0

      var j = 0

      while(j < tdrs.length) {
        val tdr = tdrs(j)
        val envelope = envelopes(j)
        val makeUpGain = makeUpGains(j)
        val triggered = tdr.triggered(volume)
        if (triggered) {
          if (!envelope.isOn) //{
            envelope.triggerOn()
            //envelopeTime = 0.0f
            //println("triggered")
          //} else
            //envelopeTime += 1.0f / sampleRate
        } else {
          if (envelope.isOn) //{
            envelope.triggerOff()
            //envelopeTime = 0.0f
            //println("triggered off")
          //} else
            //envelopeTime += 1.0f / sampleRate
        }

        //val env = envelope.apply(envelopeTime)
        val env = envelope.apply(1.0f / sampleRate)
        //println(s"env $env volume $volume envelopeTime $envelopeTime")

        val l1 = tdr.apply(l0, volume)
        val r1 = tdr.apply(r0, volume)

        val oneMinusEnv = 1.0f - env

        lOut += makeUpGain * (env * l1 + oneMinusEnv * l0)
        rOut += makeUpGain * (env * r1 + oneMinusEnv * r0)

        j += 1
      }

      lBufferOut.b(llw) = lOut
      rBufferOut.b(rlw) = rOut

      llw = lBufferOut.nextIndex(llw)
      rlw = rBufferOut.nextIndex(rlw)
      lInReadPtr = lIn.nextIndex(lInReadPtr)
      rInReadPtr = rIn.nextIndex(rInReadPtr)
      controlReadPtr = control.nextIndex(controlReadPtr)
      i += 1
    }

    lBufferOut.commitWrite(howMuch)
    rBufferOut.commitWrite(howMuch)
    lIn.commitRead(howMuch, lInReaderId)
    rIn.commitRead(howMuch, rInReaderId)
    control.commitRead(howMuch, controlReaderId)
    howMuch
  }
}
