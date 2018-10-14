package idiocy.dsp.core

class EventBuffer(val sampleRate: Int) extends EventBufferIn with EventBufferOut{
  val sz = 1024
  var isReady: Boolean = false
  if(sz <= 0)
    throw new IllegalArgumentException("sz must be non zer0")
  val b: Array[Event] = new Array[Event](sz)

  {
    var i = 0
    while(i < sz) {
      b(i) = new Event(0, 0, 0, 0, 0)
      i += 1
    }
  }

  //where to write next
  private [this] var _writePtr: Int = 0
  def writePtr: Int = _writePtr

  //where to read from next
  private [this] var readPtrs: Array[Int] = Array()
  def readPtr(index: Int): Int = readPtrs(index)

  //lowest read ptr: max writePtr + 1
  private [this] var endPtr: Int = 0

  private [this] def ptrDiff(l: Int, r: Int): Int = {
    val o = if (r > l)
      (l + sz) - r
    else
      l - r
    o
  }

  private [this] def newEnd(): Int = {
    var i = 0
    var longestLength = 0
    var end = 0
    while(i < readPtrs.length) {
      val length = ptrDiff(_writePtr, readPtrs(i))
      if(length >= longestLength){
        longestLength = length
        end = readPtrs(i)
      }

      i += 1
    }
    end
  }

  def linkTo(sampleRate: Int): Int = synchronized {
    if(sampleRate != this.sampleRate)
      throw new IllegalArgumentException("incompatible sample rates; must resample")
    val out = readPtrs.length
    val oldReadPtrs = readPtrs
    readPtrs = new Array[Int](out + 1)
    Array.copy(oldReadPtrs, 0, readPtrs, 0, out)
    readPtrs(out) = endPtr
    out
  }

  def writeCapacity: Int = synchronized {
    if (endPtr == _writePtr)
      sz - 1
    else
      ptrDiff(endPtr, _writePtr + 1)
  }

  def readCapacity(index: Int): Int = synchronized {
    ptrDiff(_writePtr, readPtrs(index))
  }

  def commitWrite(howMuch: Int, ready: Boolean): Unit = synchronized {
    checkIntegrity()
    if(howMuch < 0)
      throw new IllegalArgumentException("can't write a negative amount")
    _writePtr += howMuch
    if(_writePtr >= sz)
      _writePtr -= sz
    isReady = isReady || ready
    checkIntegrity()
  }

  def commitRead(howMany: Int, index: Int) : Unit = synchronized {
    checkIntegrity()
    if(howMany < 0)
      throw new IllegalArgumentException("can't read a negative amount")
    readPtrs(index) += howMany
    if(readPtrs(index) >= sz)
      readPtrs(index) -= sz
    endPtr = newEnd()
    checkIntegrity()
  }

  def nextIndex(in: Int): Int = {
    val out = in + 1
    if(out >= sz) out - sz else out
  }

  /*
  override def nextSampleNumber(readerId: ReaderId): Long =
    if(eventReadCapacity(readerId) > 0) b(readPtr(readerId)).sampleNumber else -1
*/

  def checkIntegrity(): Unit = {
    val howMany = ptrDiff(_writePtr, readPtrs(0))
    var rp = readPtr(0)
    var j = 0
    var ps: Long = 0
    while(j < howMany) {
      var idx = j+rp
      if(idx >= sz)
        idx -= sz
      if(ps != 0){
        if(ps > b(idx).sampleNumber)
          ???
      }
      ps = b(idx).sampleNumber
      j += 1
    }
  }
}
