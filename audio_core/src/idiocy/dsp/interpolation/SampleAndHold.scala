package idiocy.dsp.interpolation

/**
  * Implemented from Comparison of Interpolation Algorithms in Real-Time Sound Processing, which says
  *
  * Pros: simple and very fast
  * Cons: broadband noise, strong audible distortion, very low quality
  */

final class SampleAndHold extends InterpolationAlgorithm {
  override def interpolate(b: Array[Float], d: Double): Float = b(d.toInt)
}
