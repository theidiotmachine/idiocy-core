package idiocy.dsp.oscillator

class BezierCurve(val p0: Float, val p1: Float, val p2: Float, val p3: Float) {
  def apply(t: Double): Float = {
    val oneMinusT = 1 - t
    val oneMinusTSquared = oneMinusT * oneMinusT
    val oneMinusTCubed = oneMinusTSquared * oneMinusT
    val tSquared = t * t
    val tCubed = tSquared * t
    (oneMinusTCubed * p0 +
      3 * oneMinusTSquared * t * p1 +
      3 * oneMinusT * tSquared * p2 +
      tCubed * p3).toFloat
  }
}
