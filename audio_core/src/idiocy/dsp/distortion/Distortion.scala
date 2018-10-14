package idiocy.dsp.distortion

/**
  * Simple stateless symmetric transformations. Because nothing here is going to make it sound cleaner, they are all
  * 'distortion' although I imagine you could put anything here.
  */
trait Distortion {
  protected [this] def distort(in: Float): Float

  def apply(in: Float): Float =
    if(in < 0)
      -distort(-in)
    else
      distort(in)
}
