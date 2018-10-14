package idiocy.dsp.core

final class SignalBuffer(e: Engine, val sampleRate: Int) extends SignalBufferIn with SignalBufferOut {
  val sz: Int = e.sz(sampleRate)
  if(sz <= 0)
    throw new IllegalArgumentException("sz must be non zero")
  val b: Array[Float] = new Array[Float](sz)

  //where to write next
  private [this] var _writePtr: Int = 0
  def writePtr: Int = _writePtr

  //where to read from next
  private [this] var readPtrs: Array[Int] = Array()
  def readPtr(index: ReaderId): Int = readPtrs(index)

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

  def linkTo(sampleRate: Int): ReaderId = synchronized {
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

  def readCapacity(index: ReaderId): Int = synchronized {
    ptrDiff(_writePtr, readPtrs(index))
  }

  def commitWrite(howMuch: Int): Unit = synchronized {
    if(howMuch < 0)
      throw new IllegalArgumentException("can't write a negative amount")
    _writePtr += howMuch
    if(_writePtr >= sz)
      _writePtr -= sz
  }

  def commitRead(howMuch: Int, index: ReaderId) : Unit = synchronized {
    if(howMuch < 0)
      throw new IllegalArgumentException("can't read a negative amount")
    readPtrs(index) += howMuch
    if(readPtrs(index) >= sz)
      readPtrs(index) -= sz
    endPtr = newEnd()
  }

  def nextIndex(in: Int): Int = {
    val out = in + 1
    if(out >= sz) out - sz else out
  }

  override def readEndStubSize(toRead: Int, index: ReaderId): Int = {
    val rl = readPtrs(index)
    math.min(sz - rl, toRead)
  }

  override def readBeginStubSize(endStubSize: Int, toRead: Int): Int = {
    toRead - endStubSize
  }

  override def writeEndStubSize(toWrite: Int): Int = {
    val wl = writePtr
    math.min(sz - wl, toWrite)
  }

  override def writeBeginStubSize(endStubSize: Int, toWrite: Int): Int = {
    toWrite - endStubSize
  }

  override def numReaders: ReaderId = readPtrs.length
}
