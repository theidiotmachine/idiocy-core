package idiocy.dsp.core

class ConstSignalBuffer(val value: Float) extends SignalBufferIn {
  override val b: Array[Float] = Array(value)

  override def readEndStubSize(toRead: Int, index: ReaderId): Int = 1

  override def readBeginStubSize(endStubSize: Int, toRead: Int): Int = 0

  override val sz: Int = 1

  override def readPtr(index: ReaderId): Int = 0

  override def linkTo(sampleRate: Int): ReaderId = 0

  override def readCapacity(index: ReaderId): Int = 1000000000 //a large number

  override def commitRead(howMuch: Int, index: ReaderId): Unit = {}

  override def nextIndex(in: Int): Int = 0

  override def sampleRate: Int = 0

  override def numReaders: ReaderId = 1 //any number greater than zero
}
