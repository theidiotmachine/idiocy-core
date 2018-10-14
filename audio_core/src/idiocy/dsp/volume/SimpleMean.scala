package idiocy.dsp.volume

class SimpleMean(val windowSz: Int) extends VolumeDetector {
  private [this] var runningMean: Float = 0
  override def apply(x: Float): Float = {
    runningMean -= window(windowPtr)
    val n = math.abs(x) / windowSz
    window(windowPtr) = n
    runningMean += n
    nextWindowPtr()
    runningMean
  }
}
