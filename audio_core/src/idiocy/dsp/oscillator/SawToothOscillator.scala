package idiocy.dsp.oscillator

class SawToothOscillator(var phase: Double) extends Oscillator {
  override def apply(dp: Double): Float = {
    phase += dp
    phase %= 1
    phase.toFloat
  }
}