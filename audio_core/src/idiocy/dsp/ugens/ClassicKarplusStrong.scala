package idiocy.dsp.ugens

import idiocy.dsp.core._
import idiocy.dsp.envelope.{Envelope, EnvelopeHardASR}

/**
  * an implementation of the classic Karplus Strong algorithm because man
  */
class ClassicKarplusStrong(e: Engine,
                           events: EventBufferIn,
                           val masterGain: SignalBufferIn,
                           val synch: SignalBufferIn,
                           val envelope: EnvelopeHardASR,
                           sampleRate: Int) extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate)
  //longest buffer is lowest note. You want LFO, go somewhere else
  private [this] val buffer = new Array[Float]((sampleRate/Frequency.noteIdToFrequency(0)).toInt)
  //private [this] val eventReaderId = events.linkTo(sampleRate)
  private [this] val eventReader = new EventReader(events, sampleRate)

  private [this] val masterGainReaderId = masterGain.linkTo(sampleRate)
  private [this] val synchReaderId = synch.linkTo(sampleRate)
  private [this] var sampleNumber: Long = 0
  private [this] var freq: Float = 0
  private [this] var buffSz: Int = 0
  private [this] var prevBuffLoc = 0
  private [this] var buffLoc: Int = 0
  //private [this] var envelopeState: Int = 0
  //private [this] var envelopeTime: Float = 0
  private [this] var playing = false

  private [this] def resetBuffer(): Unit = {
    buffSz = (sampleRate.toFloat / freq).toInt
    var i = 0
    while(i < buffSz){
      buffer(i) = if(Math.random() > 0.5) 1 else -1
      i += 1
    }
    buffLoc = 0
    prevBuffLoc = buffSz - 1
    playing = true
  }

  override def runInternal(): Int = {
    val howMuch = calcHowMuch

    var i = 0
    var ol = bufferOut.writePtr

    var igl = masterGain.readPtr(masterGainReaderId)
    val howMany = eventReader.calcHowMany()
    var howManyEventsRead = 0

    while(i < howMuch
      //&& howManyEventsRead < howMany
    ) {
      if(howManyEventsRead < howMany && eventReader.nextSampleNumber < sampleNumber) {
        ???
      }

      while (howManyEventsRead < howMany && eventReader.nextSampleNumber == sampleNumber) {
        val ev = eventReader.event
        eventReader.next()

        ev.code match {
          case Event.EventCodePlayNote =>
            val noteId = ev.noteId
            val velocity = ev.velocity
            val volume = ev.volume
            //envelopeTime = 0
            //envelopeState = 0
            envelope.triggerOn((1.0f - velocity) * 0.01f, volume)

            freq = Frequency.noteIdToFrequency(noteId)
            resetBuffer()

          case Event.EventCodeStopNote =>

            val release = ev.release
            //envelopeTime = 0
            //envelopeState = 1
            envelope.triggerOff((1.0f-release) * 0.5f)
        }
        howManyEventsRead += 1
      }
      /*
      val env = if(envelopeState == 0)
        envelope.on(envelopeTime)
      else
        envelope.off(envelopeTime)
        */
      //val env = envelope.apply(envelopeTime)
      val env = envelope.apply(1.toFloat / sampleRate)

      buffer(buffLoc) = 0.5f * (buffer(buffLoc) + buffer(prevBuffLoc))
      bufferOut.b(ol) = masterGain.b(igl) * buffer(buffLoc) * env

      //envelopeTime += 1.toFloat / sampleRate
      igl = masterGain.nextIndex(igl)
      ol = bufferOut.nextIndex(ol)
      sampleNumber += 1
      i += 1
      prevBuffLoc = buffLoc
      buffLoc += 1
      if(buffLoc >= buffSz)
        buffLoc = 0
    }
    bufferOut.commitWrite(howMuch)
    masterGain.commitRead(howMuch, masterGainReaderId)
    synch.commitRead(howMuch, synchReaderId)
    eventReader.commitRead(howManyEventsRead)
    howMuch
  }

  private [this] def calcHowMuch: Int = {
    if(!events.isReady)
      0
    else {
      math.min(bufferOut.writeCapacity,
        math.min(masterGain.readCapacity(masterGainReaderId),
          synch.readCapacity(synchReaderId))
      )
    }
  }
}
