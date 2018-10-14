package idiocy.dsp.filter

/**
  * From Tarsos, (where it's called LowPassFSFilter) which in turn came from The Scientist and Engineer's Guide to
  * Digital Signal Processing Recursive Filters.
  *
  * Four stage low pass filter.
  *
  * Idiocy notes - I find this to be pretty unstable. Use with care.
  */
class LowPassFourStageFilter(val sampleRate: Int, override var freq: Float) extends GenericIIRFilter( 1, 4) {
  if(freq < 60)
    throw new IllegalArgumentException("min freq is 60")
  override def calcCoeff(): Unit = {
    val freqFrac = freq / sampleRate
    val x = Math.exp(-14.445 * freqFrac).toFloat
    a(0) = Math.pow(1 - x, 4).toFloat
    b(0) = 4 * x
    b(1) = -6 * x * x
    b(2) = 4 * x * x * x
    b(3) = -x * x * x * x
  }

  override def setArg2(a2: Float): Unit = {}
}
