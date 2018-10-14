package idiocy.dsp.interpolation

/**
  * An interpolation algorithm consumes an array of floats, and a fractional number, and returns a float that represents
  * an interpolated value from that array. The number is scaled so that 0 .. b.length-1 represents a complete cycle
  * across the array.
  */
trait InterpolationAlgorithm {
  def interpolate(b: Array[Float], d: Double): Float
  protected [this] def raw(b: Array[Float], d: Double): Float = {
    val finalD = if(d < 0)
      d + b.length
    else if(d >= b.length)
      d - b.length
    else
      d
    b(finalD.toInt)
  }
}
