package idiocy.dsp.oscillator

class SineOscillator(var phase: Double) extends Oscillator {
  override def apply(dp: Double): Float = {
    phase += dp
    phase %= 1
    math.sin(phase * 2.0 * math.Pi).toFloat
  }
}