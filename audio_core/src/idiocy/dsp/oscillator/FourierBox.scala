package idiocy.dsp.oscillator

class FourierBox(var phase: Double, oscs: Array[ArbitraryCosineOscillator], numCycles: Int) extends Oscillator {
  private [this] var _oscs = new Array[ArbitraryCosineOscillator](oscs.length)

  {
    var i = 0
    while(i < oscs.length){
      _oscs(i) = new ArbitraryCosineOscillator(oscs(i).phase + phase, oscs(i).omega, oscs(i).amplitude, oscs(i).offset)
      i += 1
    }
  }
  override def apply(dp: Double): Float = {
    phase += dp

    var i = 0
    var out = 0.0f
    while(i < _oscs.length){
      out += _oscs(i).apply(dp / numCycles)
      i += 1
    }
    out
  }
}
