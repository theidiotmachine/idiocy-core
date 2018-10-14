package idiocy.dsp.filter

/**
  * This came from Tarsos. I'm not sure if bandwidth is in octaves or hz. Presumably hz.
  *
  * A band pass filter is a filter that filters out all frequencies except for
  * those in a band centered on the current frequency of the filter.
  *
  * @author Damien Di Fede
  *
  */
class BandPassFilter(val sampleRate: Int, override var freq: Float, var bandWidth: Float)
  extends GenericIIRFilter( 3, 2) {
  override def calcCoeff(): Unit = {
    var bw = bandWidth / sampleRate
    val R = 1 - 3 * bw
    val fracFreq = freq / sampleRate
    val T = 2 * Math.cos(2 * Math.PI * fracFreq).toFloat
    val K = (1 - R * T + R * R) / (2 - T)
    a(0) = 1 - K
    a(1) = (K - R) * T
    a(2) = R * R - K

    b(0) = R * T
    b(1) = -R * R
  }

  override def setArg2(a2: Float): Unit = bandWidth = a2
}
