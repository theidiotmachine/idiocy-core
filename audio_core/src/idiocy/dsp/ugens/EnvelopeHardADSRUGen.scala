package idiocy.dsp.ugens

import idiocy.dsp.core._
import idiocy.dsp.envelope.EnvelopeHardADSR

class EnvelopeHardADSRUGen(e: Engine,
                           val eventBufferIn: EventBufferIn,
                           val attackCalc: Event => Float,
                           val decayCalc: Event => Float,
                           val sustainCalc: Event => Float,
                           val releaseCalc: Event => Float,
                           val synch: SignalBufferIn,
                           sampleRate: Int,
                           val mod: Float => Float = x=>x
                      ) extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate)

  private [this] val eventReader = new EventReader(eventBufferIn, sampleRate)

  private [this] val synchReaderId: Int = synch.linkTo(sampleRate)
  private [this] var sampleNumber: Long = 0
  //private [this] var envelopeTime: Float = 0
  private [this] val envelope: EnvelopeHardADSR = new EnvelopeHardADSR

  private [this] def calcHowMuch: Int = {
    if(!eventBufferIn.isReady)
      0
    else {
      val howMuch = math.min(bufferOut.writeCapacity, synch.readCapacity(synchReaderId))
      howMuch
    }
  }

  override def runInternal(): Int = {
    val howMuch = calcHowMuch
    var sr = synch.readPtr(synchReaderId)
    var ol = bufferOut.writePtr

    var i = 0
    val howMany = eventReader.calcHowMany()
    var howManyEventsRead = 0

    while(i < howMuch
      //&& howManyEventsRead < howMany
    ) {
      if (howManyEventsRead < howMany && eventReader.nextSampleNumber < sampleNumber) {
        ???
      }
      //take elements from each event buffer
      while (howManyEventsRead < howMany && eventReader.nextSampleNumber == sampleNumber) {
        val ev = eventReader.event
        eventReader.next()

        ev.code match {
          case Event.EventCodePlayNote =>
            //envelopeTime = 0
            envelope.triggerOn(attackCalc(ev), decayCalc(ev), sustainCalc(ev))
            howManyEventsRead += 1

          case Event.EventCodeStopNote =>
            //envelopeTime = 0
            envelope.triggerOff(releaseCalc(ev))
            howManyEventsRead += 1
        }
      }

      //val out = envelope.apply(envelopeTime)
      val out = envelope.apply(1.0f / sampleRate.toFloat)

      //envelopeTime += 1.0f / sampleRate.toFloat

      bufferOut.b(ol) = mod(out)
      ol = bufferOut.nextIndex(ol)
      sr = synch.nextIndex(sr)
      sampleNumber += 1
      i += 1
    }

    bufferOut.commitWrite(howMuch)
    eventReader.commitRead(howManyEventsRead)
    synch.commitRead(howMuch, synchReaderId)
    howMuch
  }
}
