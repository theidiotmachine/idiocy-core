package idiocy.dsp.interpolation

/**
  * Implemented from Comparison of Interpolation Algorithms in Real-Time Sound Processing, which says
  *
  * Pros: simple method, good results
  * Cons: noise, audible distortion in higher frequencies, requires multiplication operation
  *
  * Idiocy note: because of how I have implemented this, you should probably take the speed advisories with a pinch of
  * salt. We have no fast 'fractional part operator' and use % 1, which we are at the mercy of the JVM to optimise.
  * We are going through a vtable lookup which may well overshadow a single multiplication. And these days, float
  * operations are very very fast. So.
  */

final class Lerp extends InterpolationAlgorithm {
  override def interpolate(b: Array[Float], d: Double): Float = {
    val pf = (d % 1).toFloat
    val s0: Float = b(d.toInt)
    val s1: Float = raw(b, d+1)
    s0 + (s1 - s0) * pf
  }
}
