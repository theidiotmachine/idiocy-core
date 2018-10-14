package idiocy.dsp.filter

/**
  * This came from Tarsos, where it's called LowPassSPFilter.
  *
  * Single pass low pass filter.
  *
  * @author Joren Six
  */
class LowPassSinglePassFilter(val sampleRate: Int, override var freq: Float) extends GenericIIRFilter( 1, 1){

  override def calcCoeff(): Unit = {
    val fracFreq = freq / sampleRate
    val x = Math.exp(-2 * Math.PI * fracFreq).toFloat
    a(0) = 1 - x
    b(0) = x
  }

  override def setArg2(a2: Float): Unit = {}
}
