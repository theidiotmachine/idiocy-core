package idiocy.dsp.distortion

/**
  * Quantize the result. Try 2<<8 for that low bit feel.
  * @param q How many levels of quantization
  */
class QuantizingDistortion(val q: Float) extends Distortion {
  override protected[this] def distort(in: Float): Float = (in * q).toInt.toFloat / q
}
