package idiocy.dsp.envelope

class EnvelopeSR() extends Envelope {
  private [this] var s: Float = 1
  private [this] var r: Float = 0
  private [this] var sC: Float = 1
  private [this] var rC: Float = 0
  private [this] var currV = 0.0f

  def this(sIn: Float, rIn: Float) = {
    this()
    s = sIn
    r = rIn
  }

  def triggerOn(): Unit = {
    sC = s
    currV = 0.0f
    mode = Envelope.ModeSustain
    envelopeTime = 0.0f
  }

  override def triggerOff(): Unit = {
    rC = r
    mode = Envelope.ModeRelease
    envelopeTime = 0.0f
  }

  def triggerOn(sIn: Float):  Unit = {
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
      case Envelope.ModeSustain => sC
      case Envelope.ModeRelease =>
        var out = if(time >= rC)
          0
        else
          sC * (rC - time) / rC
        if(out <= 0.0f) {
          mode = Envelope.ModeOff
          out = 0.0f
        }
        out
    }
    currV
  }
}
