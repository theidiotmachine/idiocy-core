package idiocy.dsp.envelope

/**
  * A soft envelope will start the attack from wherever it is, which may not be zero. This means it won't pop, but it
  * will sound blurry. Use this for that classic analogue synth wobble
  *
  * This will rise in over the 'attack' period of time to the 'sustain' level.
  */
class EnvelopeSoftASR extends Envelope{
  private [this] var a: Float = 0
  private [this] var s: Float = 1
  private [this] var r: Float = 0
  private [this] var aC: Float = 0
  private [this] var sC: Float = 1
  private [this] var rC: Float = 0
  private [this] var currV: Float = 0
  private [this] var baseV: Float = 0

  def this(aIn: Float, sIn: Float, rIn: Float) = {
    this()
    a = aIn
    s = sIn
    r = rIn
  }

  def triggerOn(): Unit = {
    baseV = currV
    sC = s

    if(currV > sC) {
      mode = Envelope.ModeRetreat
      aC = a * (currV / sC)
    } else if(currV == sC) {
      mode = Envelope.ModeSustain
    } else {
      mode = Envelope.ModeAttack
      aC = a * ((sC - currV) / sC)
    }

    envelopeTime = 0.0f
  }

  def triggerOff(): Unit = {
    baseV = currV
    if(currV < sC){
      //we didn't reach peak
      //so scale down
      rC = r * (currV / sC)
    } else {
      rC = r
    }
    mode = Envelope.ModeRelease
    envelopeTime = 0.0f
  }

  def triggerOn(aIn: Float, sIn: Float): Unit = {
    a = aIn
    s = sIn
    triggerOn()
  }

  def triggerOff(rIn: Float): Unit = {
    r = rIn
    triggerOff()
  }

  /**
    * consume how far you are into the envelope, and returns the envelope value.
    *
    * @param time Time since you started the new envelope phase.
    * @return
    */
  override def applyFull(time: Float): Float = {
    currV = mode match {
      case Envelope.ModeOff => 0
      case Envelope.ModeAttack =>
        var out = (sC - baseV) * time / aC + baseV
        if(out >= sC) {
          mode = Envelope.ModeSustain
          out = sC
        }
        out
      case Envelope.ModeRetreat =>
        var out = (baseV - sC) * (aC - time) / aC + baseV
        if(out <= sC) {
          mode = Envelope.ModeSustain
          out = sC
        }
        out
      case Envelope.ModeSustain => sC
      case Envelope.ModeRelease =>
        var out = if(time >= rC)
          0
        else
          baseV * (rC - time) / rC
        if(out <= 0.0f) {
          mode = Envelope.ModeOff
          out = 0.0f
        }
        out
    }
    currV
  }
}
