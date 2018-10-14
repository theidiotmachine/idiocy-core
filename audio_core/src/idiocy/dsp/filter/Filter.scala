package idiocy.dsp.filter

/**
  * Filters are stateful functions that consume a number and return a number. They are not processors because we use
  * them in various places, notably our reverb units. A filter processor is obviously a trivial drop-in of one
  * of these guys
  */
trait Filter {
  val sampleRate: Int
  def apply(x: Float): Float
  def calcCoeff(): Unit
  var freq: Float
  def setArg2(a2: Float): Unit
  def setArg3(a3: Float): Unit = {}
}
