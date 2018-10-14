package idiocy.dsp.oscillator

class BezierOscillator(var phase: Double, val curves: Array[BezierCurve], val numberOfPhases: Int = 1) extends Oscillator {
  private [this] val phaseOfCurve: Double = numberOfPhases.toDouble / curves.length.toDouble
  override def apply(dp: Double): Float = {
    phase += dp
    phase %= numberOfPhases.toDouble
    val cIndex = (phase / phaseOfCurve).toInt
    val t = (phase - cIndex * phaseOfCurve) / phaseOfCurve
    val out = curves(cIndex).apply(t)
    if(out > 1.0f)
      1.0f
    else if(out < -1.0f)
      -1.0f
    else
      out
  }
}
