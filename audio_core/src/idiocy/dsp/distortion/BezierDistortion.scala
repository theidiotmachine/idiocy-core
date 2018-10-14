package idiocy.dsp.distortion

import idiocy.dsp.oscillator.BezierCurve

/**
  * Yes it's true, I am addicted to Beziers. I strongly advise that your p0 == 0
  *
  */
class BezierDistortion(val b: BezierCurve) extends Distortion {

  /**
    * Creates a bezier with y0 = 0, and the other values as given.
    * @param y1 first control point
    * @param y2 second control point
    * @param y3 second end point
    * @return
    */
  def this(y1: Float, y2: Float, y3: Float) = {
    this({
      val p1y = (18 * y1 - 9 * y2 + 2 * y3) / 6
      val p2y = (- 9 * y1 + 18 * y2 - 5 * y3) / 6
      new BezierCurve(0, p1y, p2y, y3)
    })
  }

  override protected[this] def distort(in: Float): Float = b.apply(in.toDouble)
}
