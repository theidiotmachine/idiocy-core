package idiocy.dsp.core

trait EventBufferIn {
  type ReaderId = Int
  val sz: Int
  val b: Array[Event]
  def readPtr(index: ReaderId): Int
  def linkTo(sampleRate: Int): Int
  def sampleRate: Int
  def readCapacity(index: ReaderId): Int
  def commitRead(howMany: Int, index: ReaderId) : Unit
  def nextIndex(in: Int): Int

  var isReady: Boolean
}
