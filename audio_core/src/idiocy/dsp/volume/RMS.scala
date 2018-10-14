package idiocy.dsp.volume

class RMS(val windowSz: Int) extends VolumeDetector {
  private [this] var runningMean: Float = 0
  override def apply(x: Float): Float = {
    runningMean -= window(windowPtr)
    val n = (x*x)/windowSz
    window(windowPtr) = n
    runningMean += n
    nextWindowPtr()
    if(runningMean<0)
      0
    else
      math.sqrt(runningMean).toFloat
  }
}
