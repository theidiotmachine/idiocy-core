package idiocy.dsp.oscillator

trait Oscillator {
  var phase: Double
  def apply(dp: Double): Float
}
