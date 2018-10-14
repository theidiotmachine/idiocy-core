package idiocy.dsp.volume

/**
  * Volume detectors are stateful functions that track a window of data to produce a volume for it.
  *
  * Realistically you will probably use RMS
  */
trait VolumeDetector {
  val windowSz: Int
  def apply(x: Float): Float

  protected [this] val window = new Array[Float](windowSz)
  protected [this] var windowPtr: Int = 0
  protected [this] def nextWindowPtr(): Unit = {
    windowPtr += 1
    if(windowPtr >= windowSz) {
      windowPtr = 0
    }
  }
}
