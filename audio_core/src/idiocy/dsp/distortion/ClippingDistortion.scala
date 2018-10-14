package idiocy.dsp.distortion

/**
  * Very simple clipping distortion
  * @param clip 0.0f-1.0f, although realistically somewhere around 0.9f
  */
class ClippingDistortion(clip: Float) extends Distortion {
  override protected[this] def distort(in: Float): Float = math.min(clip, in)
}
