package idiocy.dsp.filter

/**From Cookbook formulae for audio EQ biquad filter coefficients by Robert Bristow-Johnson.
  *
  * @param sampleRate sample rate
  * @param freq       the frequency
  * @param q          A*Q is the classic EE Q. That adjustment in definition was made so that
  *                   a boost of N dB followed by a cut of N dB for identical Q and
  *                   f0/Fs results in a precisely flat unity gain filter or "wire". Remember that bandwidth = freq / q
  * @param dbGain     db Gain
  */
class PeakingEQBiQuadFilter(val sampleRate: Int, override var freq: Float, var q: Float, var dbGain: Float)
  extends GenericIIRFilter( 3, 2)  {

  override def calcCoeff(): Unit = {
    val w0 = 2 * math.Pi * freq / sampleRate
    val alpha = Math.sin(w0)/(2*q)
    val cosw0 = Math.cos(w0)
    val A = Math.pow(10, dbGain/40)

    val b0 =   1 + alpha*A
    val b1 =  -2*cosw0
    val b2 =   1 - alpha*A
    val a0 =   1 + alpha/A
    val a1 =  -2*cosw0
    val a2 =   1 - alpha/A

    a(0) = (b0/a0).toFloat
    a(1) = (b1/a0).toFloat
    a(2) = (b2/a0).toFloat
    b(0) = -(a1/a0).toFloat
    b(1) = -(a2/a0).toFloat
  }

  override def setArg2(a2: Float): Unit = q = a2
  override def setArg3(a3: Float): Unit = dbGain = a3
}
