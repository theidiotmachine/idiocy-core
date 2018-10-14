package idiocy.dsp.filter

/**
  * This came from Tarsos
  *
  * A High pass IIR filter. Frequency defines the cutoff.
  *
  * @author Joren Six
  */
class HighPassFilter(val sampleRate: Int, override var freq: Float) extends GenericIIRFilter( 2, 1) {
  override def calcCoeff(): Unit = {
    val fracFreq = freq / sampleRate
    val x = Math.exp(-2 * Math.PI * fracFreq).toFloat

    a(0) = (1 + x) / 2
    a(1) = -(1 + x) / 2

    b(0) = x
  }

  override def setArg2(a2: Float): Unit = {}
}
