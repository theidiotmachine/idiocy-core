package idiocy.dsp.dynamicRange

class HardKneeDownwardCompressor(val trigger: Float, val upperPoint: Float) extends TriggeredDynamicRange {
  private [this] val slope = (upperPoint - trigger) / (1.0f - trigger)
  override def triggered(x: Float): Boolean = x > trigger

  def apply(in: Float, vol: Float): Float =
    if(vol < trigger)
      in
    else {
      val mul = trigger + slope * (vol - trigger)
      in * (mul/vol)
    }
}
