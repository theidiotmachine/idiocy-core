package idiocy.dsp.interpolation

/**
  * Implemented from Comparison of Interpolation Algorithms in Real-Time Sound Processing, which says
  *
  * Pros: good results, smooth connection of adjacent curves
  * Cons: computationally intensive, requires many multiplication operations and work registers
  *
  * Idiocy note: because of how I have implemented this, you should probably take the speed advisories with a pinch of
  * salt. We have no fast 'fractional part operator' and use % 1, which we are at the mercy of the JVM to optimise.
  * We are going through a vtable lookup which may well overshadow a single multiplication. And these days, float
  * operations are very very fast.
  *
  * Having said, this guy does look pretty heavy weight. But we love it!
  */
final class CubicSplineInterpolation extends InterpolationAlgorithm {
  override def interpolate(b: Array[Float], d: Double): Float = {
    val pf = (d % 1).toFloat
    val pfsq: Float = pf * pf
    val sn1: Float = raw(b, d-1)
    val s0: Float = b(d.toInt)
    val s1: Float = raw(b, d+1)
    val s2: Float = raw(b, d+2)
    val s0d: Float = s0 - sn1
    val s1d = s2 - s1
    s0 + pf/2 * ((2*pfsq - 3*pf -1)*(s0 - s1) + (pfsq - 2*pf + 1)*s0d + (pfsq - pf)*s1d)
  }
}
