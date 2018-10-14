package idiocy.dsp.core

class EventReader(val eventBufferIn: EventBufferIn, sampleRate: Int) {
  var nextSampleNumber: Long = -1

  private [this] val readerId: Int = eventBufferIn.linkTo(sampleRate)
  private [this] var readPtr: Int = eventBufferIn.readPtr(readerId)
  def calcHowMany(): Int = {
    if(eventBufferIn.isReady){
      //teeny bit naughty: take the opportunity to init next sample number
      if(nextSampleNumber == -1)
        nextSampleNumber = eventBufferIn.b(readPtr).sampleNumber
      //-1 means we have the next sample number
      eventBufferIn.readCapacity(readerId) - 1
    }
    else
      0
  }
  def next(): Unit = {
    readPtr = eventBufferIn.nextIndex(readPtr)
    nextSampleNumber = eventBufferIn.b(readPtr).sampleNumber
  }
  def event: Event = eventBufferIn.b(readPtr)

  def commitRead(howMany: Int): Unit = eventBufferIn.commitRead(howMany, readerId)
}
