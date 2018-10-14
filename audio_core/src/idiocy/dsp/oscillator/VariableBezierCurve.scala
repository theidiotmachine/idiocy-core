package idiocy.dsp.oscillator

class VariableBezierCurve(val p0Oscs: Array[Oscillator],
                          val y1Oscs: Array[Oscillator],
                          val y2Oscs: Array[Oscillator],
                          val p3Oscs: Array[Oscillator]) {

  def this(p0Osc: Oscillator,
           y1Osc: Oscillator,
           y2Osc: Oscillator,
           p3Osc: Oscillator) = {
    this(Array(p0Osc), Array(y1Osc), Array(y2Osc), Array(p3Osc))
  }

  private [this] def runOsc(oscs: Array[Oscillator], dCycle: Double): Float = {
    var i = 0
    var out = 0.0f
    while(i < oscs.length){
      out += oscs(i).apply(dCycle)
      i += 1
    }
    out
  }

  def apply(t: Double, dCycle: Double): Float = {
    val oneMinusT = 1 - t
    val oneMinusTSquared = oneMinusT * oneMinusT
    val oneMinusTCubed = oneMinusTSquared * oneMinusT
    val tSquared = t * t
    val tCubed = tSquared * t

    //this code from John Burkardt
    val y0 = runOsc(p0Oscs, dCycle)
    val y1 = runOsc(y1Oscs, dCycle)
    val y2 = runOsc(y2Oscs, dCycle)
    val y3 = runOsc(p3Oscs, dCycle)
    val p1y = (-5 * y0 + 18 * y1 - 9 * y2 + 2 * y3) / 6
    val p2y = (2 * y0 - 9 * y1 + 18 * y2 - 5 * y3) / 6

    val out = (oneMinusTCubed * y0 +
      3 * oneMinusTSquared * t * p1y +
      3 * oneMinusT * tSquared * p2y +
      tCubed * y3).toFloat
    if(out > 1.0f)
      1.0f
    else if(out < -1.0f)
      -1.0f
    else
      out
  }

  /*
  def mutate(p0: Array[Oscillator],
             p1: Array[Oscillator],
             p2: Array[Oscillator],
             p3: Array[Oscillator]): VariableBezierCurve = {
    new VariableBezierCurve(p0Oscs ++ p0, y1Oscs ++ p1, y2Oscs ++ p2, p3Oscs ++ p3)
  }*/
}
