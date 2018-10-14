package idiocy.dsp.dynamicRange

trait TriggeredDynamicRange extends DynamicRange{
  def triggered(vol: Float): Boolean
}
