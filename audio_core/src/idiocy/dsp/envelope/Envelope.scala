package idiocy.dsp.envelope

object Envelope{
  //dormant
  val ModeOff: Int = -1
  //attack, moving up to the envelope's peak
  val ModeAttack: Int = 0
  //a soft envelope can find itself in attack mode to a place lower than it is now. We call this 'retreat'
  val ModeRetreat: Int = 1
  //ADSR envelopes have a peak, followed by a drop to a sustained volume. The drop is known as the 'decay'
  val ModeDecay: Int = 2
  //The envelope is in on mode
  val ModeSustain: Int = 3
  //The envelope has been turned off, and is dropping to zero
  val ModeRelease: Int = 4
}

/**
  * Stateful envelope. Trigger new envelope phases with envelope-specific functions to vary them, or use the
  * generic triggerOn and triggerOff for statics.
  */
trait Envelope {
  var mode: Int = Envelope.ModeOff

  var envelopeTime: Float = 0.0f

  /**
    * consume how far you are into the envelope, and returns the envelope value.
    * @param time Time since you started the new envelope phase.
    * @return
    */
  protected [this] def applyFull(time: Float): Float

  /**
    * consume a delta, return the envelope value. Note, will increment after the envelope function.
    * This is because you will generally call trigger before you call this, and that makes pops otherwise
    * @param dt delta time in seconds. Almost certainly 1 / sample rate
    * @return
    */
  def apply(dt: Float): Float = {
    val o = applyFull(envelopeTime)
    envelopeTime += dt
    o
  }

  def triggerOn(): Unit
  def triggerOff(): Unit
  def isOn:  Boolean =
    mode == Envelope.ModeAttack ||
      mode == Envelope.ModeRetreat ||
      mode == Envelope.ModeDecay ||
      mode == Envelope.ModeSustain
}
