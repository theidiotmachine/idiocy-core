package idiocy.dsp.volume

/**
  * windowed peak detector using ascending minima (well, descending maxima actually, but it seems to be called that)
  * @param windowSz The window size
  */
class PeakDetector(val windowSz: Int) extends VolumeDetector {

  //I made it plus one because that just makes the whole begin and end thing easier
  private [this] val maxListSz = windowSz+1
  private [this] val maxList = new Array[Float](maxListSz)
  private [this] var maxListStart = 0
  private [this] var maxListEnd = 0
  private [this] var windowUsedSz = 0

  private [this] def nextIdx(idx: Int): Int = {
    val o = idx + 1
    if(o >= maxListSz)
      0
    else
      o
  }

  private [this] def appendToEnd(x: Float): Unit = {
    var i = maxListStart
    var looking = true
    while(looking && i != maxListEnd){
      if(x > maxList(i)){
        //cut here
        maxList(i) = x
        maxListEnd = nextIdx(i)
        looking = false
      } else
        i = nextIdx(i)
    }
    if(looking) {
      maxList(i) = x
      maxListEnd = nextIdx(i)
    }
  }

  override def apply(x: Float): Float = {
    val oldN = window(windowPtr)
    val n = math.abs(x)
    window(windowPtr) = n
    val stillFillingWindow = windowUsedSz < windowSz
    windowUsedSz += 1
    if(windowUsedSz > windowSz)
      windowUsedSz = windowSz
    val out = if(stillFillingWindow){
      appendToEnd(x)
      maxList(maxListStart)
    } else {
      if(oldN == maxList(maxListStart))
        maxListStart = nextIdx(maxListStart)
      appendToEnd(x)
      maxList(maxListStart)
    }

    nextWindowPtr()
    out
  }
}
