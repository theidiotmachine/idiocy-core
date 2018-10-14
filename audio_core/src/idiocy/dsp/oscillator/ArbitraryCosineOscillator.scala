package idiocy.dsp.oscillator

/**
  * Full blown arbitrary cosine oscillator that doesn't normalize anything. Note that a dp here is over 0-1.
  * we assume you put the 2 pi into the omega. But the phase will be 0 - 2 Pi
  *
  * Is essentially offset + (amplitude * cosine(x * omega + phase))
  *
  * @param phase initial phase
  * @param omega wavelength modifier
  * @param amplitude amplitude modifier
  */
class ArbitraryCosineOscillator(var phase: Double, val omega: Float, val amplitude: Float, val offset: Float) extends Oscillator {
  override def apply(dp: Double): Float = {
    phase += dp * omega
    phase %= 2.0f * math.Pi
    offset + amplitude * math.cos(phase).toFloat
  }
}
