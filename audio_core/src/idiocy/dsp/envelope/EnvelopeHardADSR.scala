package idiocy.dsp.envelope

class EnvelopeHardADSR extends Envelope {
  private [this] var a: Float = 0
  private [this] var d: Float = 0
  private [this] var s: Float = 1
  private [this] var r: Float = 0
  private [this] var aC: Float = 0
  private [this] var dC: Float = 0
  private [this] var sC: Float = 1
  private [this] var rC: Float = 0
  private [this] var currV: Float = 0
  private [this] var baseV: Float = 0

  def this(aIn: Float, dIn: Float, sIn: Float, rIn: Float) = {
    this()
    a = aIn
    d = dIn
    s = sIn
    r = rIn
  }

  override def triggerOn():  Unit = {
    aC = a
    dC = d
    sC = s
    currV = 0.0f
    baseV = 0.0f
    mode = Envelope.ModeAttack
    envelopeTime = 0.0f
  }

  override def triggerOff(): Unit = {
    baseV = currV
    if(currV < sC){
      //we didn't reach peak
      //so scale down
      rC = r * (currV / sC)
      sC = currV
    } else if(currV > sC) {
      rC = r * (currV / sC)
      sC = currV
    } else
      rC = r

    envelopeTime = 0.0f
    mode = Envelope.ModeRelease
  }

  def triggerOn(aIn: Float, dIn: Float, sIn: Float):  Unit = {
    a = aIn
    d = dIn
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
        var out = 1.0f * time / aC
        if(out >= 1.0f) {
          mode = Envelope.ModeDecay
          out = 1.0f
        }
        out
      case Envelope.ModeDecay =>
        val dtime = time-aC
        val dqty = dtime / dC
        val dmin = 1-sC
        var out = dmin * (1 - dqty) +  sC
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
