package idiocy.dsp.dynamicRange

class Limiter(val trigger: Float) extends TriggeredDynamicRange {
  override def triggered(vol: Float): Boolean = vol > trigger

  def apply(in: Float, vol: Float): Float = {
    if(vol > trigger)
      //math.min(triggerVal, in)
      in * (trigger / vol)
    else
      in
  }
}
