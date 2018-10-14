package idiocy.dsp.dynamicRange

trait DynamicRange {
  //def apply(in: Float, vol: Float): Float = if(in < 0) -applyInternal(-in, vol) else applyInternal(in, vol)

  def apply(in: Float, vol: Float): Float
}
