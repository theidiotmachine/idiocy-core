package idiocy.dsp.distortion

/**
  * From https://dsp.stackexchange.com/questions/13142
  */
class ExponentialDistortion extends Distortion {
  override protected[this] def distort(in: Float): Float = 1.0f - math.exp(-in).toFloat
}
