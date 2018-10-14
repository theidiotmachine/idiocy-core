package idiocy.dsp.ugens

import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.oscillator.Oscillator

/**
  * Generic osclillator
  * @param e the engine
  * @param masterGain master gain
  * @param frequency the frequency that all the oscillators will run at
  * @param sampleRate sample rate
  * @param oscillators the oscillators - an array of Oscillator
  * @param gains an array of individual gains for the oscillators, or nothing
  * @param oscillatorMods stateless formula to apply to the oscillators
  * @param gainMods stateless formula to apply to the gains.
  */
class OscillatorsUGen(e: Engine,
                      val masterGain: SignalBufferIn,
                      val frequency: SignalBufferIn,
                      sampleRate: Int,
                      val oscillators: Array[Oscillator],
                      val gains: Array[SignalBufferIn] = Array(),
                      val oscillatorMods: Array[Float => Float] = Array(),
                      val gainMods: Array[Float => Float] = Array()
                     ) extends UGen(e) with UGenSignalOut1{
  initOut(e, sampleRate)
  private [this] val masterGainReaderId = masterGain.linkTo(sampleRate)
  private [this] val frequencyReaderId = frequency.linkTo(sampleRate)
  private [this] val hasOscillatorMods = oscillatorMods.length != 0
  private [this] val gainReaderIds: Array[Int] = new Array(gains.length)
  private [this] val hasGains = gains.length != 0
  private [this] val hasGainMods = gainMods.length != 0
  private [this] val numOsc = oscillators.length
  private [this] val invNumOsc: Float = 1.0f / numOsc.toFloat

  {
    if(hasGains){
      var i = 0
      while(i < gains.length){
        gainReaderIds(i) = gains(i).linkTo(sampleRate)
        i += 1
      }

      if(hasOscillatorMods)
        if(gainMods.length != gains.length)
          throw new IllegalArgumentException("if you have gains and you have oscillatorMods you must have gainMods")
    }
  }

  //irritating buffers so I don't need to realloc every cycle
  private [this] val gainReadPtrs = new Array[Int](gains.length)

  override def runInternal(): Int = {
    val howMuch = math.min(bufferOut.writeCapacity, math.min(masterGain.readCapacity(masterGainReaderId), frequency.readCapacity(frequencyReaderId)))

    var i = 0
    var ol = bufferOut.writePtr
    var ifl = frequency.readPtr(frequencyReaderId)
    var masterGainReadPtr = masterGain.readPtr(masterGainReaderId)

    var j = 0
    while(j < numOsc){
      if(hasGains)
        gainReadPtrs(j) = gains(j).readPtr(gainReaderIds(j))
      j += 1
    }

    while(i < howMuch) {
      var j = 0
      val f = frequency.b(ifl)
      val dp: Double = f.toDouble / sampleRate.toDouble

      var thisOut: Float = 0
      while(j < numOsc) {
        val thisOutForOsc: Float = if(hasOscillatorMods)
          oscillatorMods(j)(oscillators(j).apply(dp)) * invNumOsc
        else
          oscillators(j).apply(dp) * invNumOsc
        val thisGain = if(hasGains) {
          if(hasGainMods)
            gainMods(j)(gains(j).b(gainReadPtrs(j)))
          else
            gains(j).b(gainReadPtrs(j))
        } else
          1.0f
        thisOut += thisGain * thisOutForOsc

        if(hasGains)
          gainReadPtrs(j) = gains(j).nextIndex(gainReadPtrs(j))

        j += 1
      }
      bufferOut.b(ol) = masterGain.b(masterGainReadPtr) * thisOut
      ol = bufferOut.nextIndex(ol)
      ifl = frequency.nextIndex(ifl)
      masterGainReadPtr = masterGain.nextIndex(masterGainReadPtr)
      i += 1
    }

    j = 0
    while(j < numOsc){
      if(hasGains)
        gains(j).commitRead(howMuch, gainReaderIds(j))
      j += 1
    }
    bufferOut.commitWrite(howMuch)
    frequency.commitRead(howMuch, frequencyReaderId)
    masterGain.commitRead(howMuch, masterGainReaderId)
    howMuch
  }
}
