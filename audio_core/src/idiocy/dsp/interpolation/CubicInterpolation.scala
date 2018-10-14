package idiocy.dsp.interpolation

/**
  * Implemented from Comparison of Interpolation Algorithms in Real-Time Sound Processing, which says
  *
  * Pros: usable results
  * Cons: computationally expensive, requires many multiplication operations and work registers, sharp edges between
  * adjacent curves.
  *
  * Idiocy note: because of how I have implemented this, you should probably take the speed advisories with a pinch of
  * salt. We have no fast 'fractional part operator' and use % 1, which we are at the mercy of the JVM to optimise.
  * We are going through a vtable lookup which may well overshadow a single multiplication. And these days, float
  * operations are very very fast.
  *
  * Having said, this guy does look pretty heavy weight
  */

final class CubicInterpolation extends InterpolationAlgorithm {
  override def interpolate(b: Array[Float], d: Double): Float = {
    val pf = (d % 1).toFloat
    val dint = d.toInt
    val pfsq: Float = pf * pf
    val sn1: Float = raw(b, d-1)
    val s0: Float = b(dint)
    val s1: Float = raw(b, d+1)
    val s2: Float = raw(b, d+2)

    s0 + pf/6 *((-pfsq + 3*pf - 2)*sn1 + 3*(pfsq - 2*pf -1)*s0 + 3*(-pfsq + pf + 2)*s1 + (pfsq -1) *s2)
  }
}
