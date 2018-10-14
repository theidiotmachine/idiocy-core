package idiocy.dsp.filter

/**
  * From Tarsos, although fairly heavily modified
  *
  * An Infinite Impulse Response, or IIR, filter is a filter that uses a set of
  * coefficients and previous filtered values to filter a stream of audio. It is
  * an efficient way to do digital filtering. IIRFilter is a general IIRFilter
  * that simply applies the filter designated by the filter coefficients so that
  * sub-classes only have to dictate what the values of those coefficients are by
  * defining the <code>calcCoeff()</code> function. When filling the coefficient
  * arrays, be aware that <code>b[0]</code> corresponds to
  * <code>b<sub>1</sub></code>.
  *
  * @author Damien Di Fede
  * @author Joren Six
  *
  */
abstract class GenericIIRFilter(inSz: Int, outSz: Int) extends Filter {
  protected [this] val in: Array[Float] = new Array(inSz)
  protected [this] val out: Array[Float] = new Array(outSz)
  protected [this] val a: Array[Float] = new Array(inSz)
  protected [this] val b: Array[Float] = new Array(outSz)
  protected [this] var inIdx = 0
  protected [this] var outIdx = 0

  calcCoeff()

  def apply(x: Float): Float = {
    if(x.isInfinity)
      ???
    in(inIdx) = x
    var y: Float = 0
    var i = 0
    var idx = inIdx
    while (i < a.length) {
      y += a(i) * in(idx)
      idx -= 1
      if(idx < 0)
        idx += a.length
      i += 1
    }
    i = 0
    idx = outIdx
    while (i < b.length) {
      y += b(i) * out(idx)
      idx -= 1
      if(idx < 0)
        idx += b.length
      i += 1
    }
    if(y > 1)
      y = 1
    else if(y < -1)
      y = -1

    outIdx += 1
    if(outIdx == b.length)
      outIdx = 0

    out(outIdx) = y

    inIdx += 1
    if(inIdx == a.length)
      inIdx = 0

    if(y.isNaN || y.isInfinity)
      ???
    y
  }
}
