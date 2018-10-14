package idiocy.dsp.oscillator

import idiocy.dsp.interpolation.InterpolationAlgorithm

class WaveTable(var phase: Double, val table: Array[Float], val interpolationAlgorithm: InterpolationAlgorithm) extends Oscillator {
  override def apply(dp: Double): Float = {
    phase += dp
    phase %= 1.0f
    interpolationAlgorithm.interpolate(table, phase * table.length)
  }
}
