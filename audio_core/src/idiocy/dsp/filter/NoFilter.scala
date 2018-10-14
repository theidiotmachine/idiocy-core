package idiocy.dsp.filter

class NoFilter extends Filter {
  override val sampleRate: Int = 0

  override def apply(x: Float): Float = x

  override def calcCoeff(): Unit = {}

  override def setArg2(a2: Float): Unit = {}

  override var freq: Float = 0
}
