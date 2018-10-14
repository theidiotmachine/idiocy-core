package idiocy.dsp.ugens

import idiocy.dsp.core._

class SimpleTestTracker(e: Engine, sampleRate: Int, noteTime: Double = 0.5, noteLen: Double = 0.6)
  extends UGen(e) with UGenSignalOut0{
  private [this] val b = new EventBuffer(sampleRate)
  private [this] val bOut: EventBufferOut = b
  val out: EventBufferIn = b

  private [this] var timeNow: Double = 0
  private [this] var sampleNow: Long = 0

  val noteBottom: Int = Frequency.noteNameToNoteId("C3")
  val noteTop: Int = Frequency.noteNameToNoteId("C5")
  private [this] var noteNow = noteBottom
  private [this] var nextNoteTime: Double = 0
  private [this] var nextNoteOffTime: Double = noteLen
  private [this] var noteOffNow = noteBottom

  override def runInternal(): Int = {
    val howMany = bOut.writeCapacity
    var ew = bOut.writePtr
    var numEv = 0

    while(numEv < howMany) {
      timeNow = sampleNow.toDouble / sampleRate
      if(timeNow >= nextNoteTime) {
        val ev = bOut.b(ew)

        val l = (0.25 + (0.5 * math.random())).toFloat

        ev.playNote(sampleNow, noteNow, l + math.random().toFloat * 0.2f - 0.1f, l + math.random().toFloat * 0.2f - 0.1f)
        noteNow += 1
        if(noteNow>noteTop)
          noteNow = noteBottom
        nextNoteTime += noteTime
        ew = bOut.nextIndex(ew)
        numEv += 1
      }

      if(timeNow >= nextNoteOffTime) {
        val ev = bOut.b(ew)
        ev.stopNote(sampleNow, noteOffNow, (0.5 + (0.5*math.random())).toFloat)

        noteOffNow += 1
        if(noteOffNow>noteTop)
          noteOffNow = noteBottom
        nextNoteOffTime += noteTime
        ew = bOut.nextIndex(ew)
        numEv += 1
      }

      sampleNow += 1
    }

    bOut.commitWrite(numEv, true)
    0 //???
  }
}
