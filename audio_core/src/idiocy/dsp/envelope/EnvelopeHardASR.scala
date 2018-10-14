package idiocy.dsp.envelope

/**
  * A hard envelope forces attack to start at zero. You need this if you are dealing with polyphonic samples, when you
  * want the thing to start with a bang. This means that if you play them too quickly after each other, they will
  * pop. This is a sign that you need to increase your polyphony.
  */
class EnvelopeHardASR() extends Envelope {
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
    aC = a
    sC = s
    currV = 0.0f
    mode = Envelope.ModeAttack
    envelopeTime = 0.0f
  }

  def triggerOn(aIn: Float, sIn: Float): Unit = {
    a = aIn
    s = sIn
    triggerOn()
  }

  def triggerOff(): Unit = {
    baseV = currV
    if(currV < sC){
      //we didn't reach peak
      //so scale down
      rC = r * (currV / sC)
      sC = currV
    } else
      rC = r
    mode = Envelope.ModeRelease
    envelopeTime = 0.0f
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
        var out = sC * time / aC
        if(out >= sC) {
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
