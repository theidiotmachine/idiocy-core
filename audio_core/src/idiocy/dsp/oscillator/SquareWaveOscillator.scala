package idiocy.dsp.oscillator

class SquareWaveOscillator(var phase: Double) extends Oscillator {
  override def apply(dp: Double): Float = {
    phase = phase + dp
    phase %= 1
    if (phase > 0.5)
      1
    else
      -1
  }
}