package idiocy.dsp.filter

/**From Cookbook formulae for audio EQ biquad filter coefficients by Robert Bristow-Johnson
  *
  * @param sampleRate sample rate
  * @param freq the frequency
  * @param q Q. remember that bandwidth = freq / q
  */
class AllPassBiQuadFilter(val sampleRate: Int, override var freq: Float, var q: Float)
  extends GenericIIRFilter(3, 2) {
  override def calcCoeff(): Unit = {
    val w0 = 2 * math.Pi * freq / sampleRate
    val alpha = Math.sin(w0)/(2*q)
    val cosw0 = Math.cos(w0)

    val b0 =   1 - alpha
    val b1 =  -2*cosw0
    val b2 =   1 + alpha
    val a0 =   1 + alpha
    val a1 =  -2*cosw0
    val a2 =   1 - alpha


    a(0) = (b0/a0).toFloat
    a(1) = (b1/a0).toFloat
    a(2) = (b2/a0).toFloat
    b(0) = -(a1/a0).toFloat
    b(1) = -(a2/a0).toFloat
  }

  override def setArg2(a2: Float): Unit = q = a2
}
