package idiocy.dsp.core

trait EventBufferOut {
  val b: Array[Event]
  def writePtr: Int
  def writeCapacity: Int
  def commitWrite(howMuch: Int, ready: Boolean): Unit

  def nextIndex(in: Int): Int
}
