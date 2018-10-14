package idiocy.dsp.ugens

import idiocy.dsp.core._
import idiocy.dsp.pitch.FastYin

class PitchDetectorUGen(e: Engine, val in: SignalBufferIn,
                        val probabilityThreshold: Float = 0.75f,
                        val fftSz: Int = 4096) extends UGen(e) with UGenSignalOut0{
  private [this] val b = new EventBuffer(in.sampleRate)
  private [this] val bOut: EventBufferOut = b
  val out: EventBufferIn = b

  val readerId: Int = in.linkTo(in.sampleRate)

  private [this] var sampleNow: Long = 0

  private [this] var prevNote = -1

  private [this] val yin: FastYin = new FastYin(fftSz, in.sampleRate)

  override def runInternal(): Int = {
    val howMany = bOut.writeCapacity
    val howMuch = in.readCapacity(readerId)

    var inReadPtr = in.readPtr(readerId)
    var ew = bOut.writePtr
    var numEv = 0
    var left = howMuch
    var endStubSize = in.readEndStubSize(howMuch, readerId)
    var beginStubSize = in.readBeginStubSize(endStubSize, howMuch)
    var end = true
    var howMuchRead = 0
    while(left >= fftSz && numEv < howMany){
      if(fftSz < endStubSize){
        Array.copy(in.b, inReadPtr, yin.buffer, 0, fftSz)
        inReadPtr += fftSz
        endStubSize -= fftSz
      }
      else if (endStubSize == 0){
        Array.copy(in.b, inReadPtr, yin.buffer, 0, fftSz)
        inReadPtr += fftSz
        beginStubSize -= fftSz
      }
      else {
        val beginStub = fftSz - endStubSize
        Array.copy(in.b, inReadPtr, yin.buffer, 0, endStubSize)
        Array.copy(in.b, 0, yin.buffer, endStubSize, beginStub)
        endStubSize = 0
        inReadPtr = beginStub
        beginStubSize -= beginStub
      }

      yin.calcPitch()

      if(yin.pitched && yin.probability > probabilityThreshold) {
        val noteId = Frequency.frequencyToNoteId(yin.pitch)
        if(noteId != -1 && prevNote != noteId) {
          val ev = bOut.b(ew)
          ev.playNote(sampleNow, noteId, 1.0f, 1.0f)
          ew = bOut.nextIndex(ew)
          numEv += 1
          prevNote = noteId
        }
      } else if(prevNote != -1) {
        val ev = bOut.b(ew)
        ev.stopNote(sampleNow, noteId = prevNote, 1.0f)
        ew = bOut.nextIndex(ew)
        numEv += 1
        prevNote = -1
      }

      sampleNow += fftSz
      howMuchRead += fftSz
      left -= fftSz
    }

    in.commitRead(howMuchRead, readerId)
    if(howMuchRead > 0)
      bOut.commitWrite(numEv, true)
    howMuch
  }
}
