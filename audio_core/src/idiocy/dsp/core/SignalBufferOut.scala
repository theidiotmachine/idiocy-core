package idiocy.dsp.core

trait SignalBufferOut {
  val b: Array[Float]
  def writePtr: Int
  def writeCapacity: Int
  def commitWrite(howMuch: Int): Unit

  def nextIndex(in: Int): Int

  def writeEndStubSize(toWrite: Int): Int
  def writeBeginStubSize(endStubSize: Int, toWrite: Int): Int
}
