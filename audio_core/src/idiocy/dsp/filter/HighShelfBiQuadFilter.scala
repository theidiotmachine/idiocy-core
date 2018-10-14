package idiocy.dsp.filter

/**From Cookbook formulae for audio EQ biquad filter coefficients by Robert Bristow-Johnson.
  * I just can;t get this guy to work
  *
  * @param sampleRate sample rate
  * @param freq       the frequency
  * @param slope      "shelf slope" parameter. When S = 1,
  *                   the shelf slope is as steep as it can be and remain monotonically
  *                   increasing or decreasing gain with frequency.  The shelf slope, in
  *                   dB/octave, remains proportional to S for all other values for a
  *                   fixed f0/Fs and dBgain.
  * @param dbGain     db Gain
  */
class HighShelfBiQuadFilter(val sampleRate: Int, override var freq: Float, var slope: Float, var dbGain: Float)
  extends GenericIIRFilter(3, 2) {

  override def calcCoeff(): Unit = {
    val w0 = 2 * math.Pi * freq / sampleRate
    val alpha = Math.sin(w0)/(2*slope)
    val cosw0 = Math.cos(w0)
    val A = Math.pow(10, dbGain/40)
    val sqrtA = Math.sqrt(A)

    val b0 =    A*( (A+1) + (A-1)*cosw0 + 2*sqrtA*alpha )
    val b1 = -2*A*( (A-1) + (A+1)*cosw0                 )
    val b2 =    A*( (A+1) + (A-1)*cosw0 - 2*sqrtA*alpha )
    val a0 =        (A+1) - (A-1)*cosw0 + 2*sqrtA*alpha
    val a1 =    2*( (A-1) - (A+1)*cosw0                 )
    val a2 =        (A+1) - (A-1)*cosw0 - 2*sqrtA*alpha

    a(0) = (b0/a0).toFloat
    a(1) = (b1/a0).toFloat
    a(2) = (b2/a0).toFloat
    b(0) = -(a1/a0).toFloat
    b(1) = -(a2/a0).toFloat
  }

  override def setArg2(a2: Float): Unit = slope = a2
  override def setArg3(a3: Float): Unit = dbGain = a3
}
