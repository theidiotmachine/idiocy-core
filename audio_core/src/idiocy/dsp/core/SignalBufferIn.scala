package idiocy.dsp.core

trait SignalBufferIn {
  type ReaderId = Int
  def readEndStubSize(toRead: Int, readerId: ReaderId): Int
  def readBeginStubSize(endStubSize: Int, toRead: Int): Int

  val sz: Int
  val b: Array[Float]
  def readPtr(readerId: ReaderId): Int
  def linkTo(sampleRate: Int): ReaderId
  def sampleRate: Int
  def readCapacity(readerId: ReaderId): Int
  def commitRead(howMuch: Int, readerId: ReaderId) : Unit
  def nextIndex(in: Int): Int

  def numReaders: Int
}
