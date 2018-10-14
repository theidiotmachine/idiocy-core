package idiocy.dsp.dynamicRange

class Gate(val trigger: Float) extends TriggeredDynamicRange {
  override def triggered(x: Float): Boolean = x < trigger

  def apply(in: Float, vol: Float): Float = {
    if(vol > trigger)
      in
    else
      0
  }
}
