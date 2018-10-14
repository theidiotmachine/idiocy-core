package idiocy.dsp.oscillator

class VariableBezierOscillator(var phase: Double, val curves: Array[VariableBezierCurve]) extends Oscillator {
  private [this] val phaseOfCurve: Double = 1.0 / curves.length.toDouble
  override def apply(dp: Double): Float = {
    val oldPhase = phase.toInt
    phase += dp
    val newPhase = phase.toInt
    val dCycle = if(newPhase > oldPhase) 1.0 else 0.0
    phase %= 1.0
    val cIndex = (phase / phaseOfCurve).toInt
    val t = (phase - cIndex * phaseOfCurve) / phaseOfCurve
    val out = curves(cIndex).apply(t, dCycle)
    if(out > 1.0f)
      1.0f
    else if(out < -1.0f)
      -1.0f
    else
      out
  }


}
