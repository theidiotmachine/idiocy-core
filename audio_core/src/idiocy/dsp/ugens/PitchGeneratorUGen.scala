package idiocy.dsp.ugens

import idiocy.dsp.core._
import idiocy.dsp.ugens.PitchGeneratorUGen.Glide

object PitchGeneratorUGen {
  trait Glide{
    def apply(reqFreq: Float, origFreq: Float, currTime: Float): Float
  }

  class NoGlide extends Glide{
    override def apply(reqFreq: Float, origFreq: Float, currTime: Float): Float = {
      reqFreq
    }
  }

  class LinearFreqGlide(val time: Float) extends Glide{
    override def apply(reqFreq: Float, origFreq: Float, currTime: Float): Float = {
      val t = math.min(currTime/time, 1)
      (1-t) * origFreq + t * reqFreq
    }
  }
}

class PitchGeneratorUGen(e: Engine,
                         eventBufferIn: EventBufferIn,
                         val glide: Glide,
                         val synch: SignalBufferIn,
                         sampleRate: Int) extends UGen(e) with UGenSignalOut1 {
  initOut(e, sampleRate)

  //private [this] val eventRufferInReaderId: Int = eventBufferIn.linkTo(sampleRate)
  private [this] val eventReader = new EventReader(eventBufferIn, sampleRate)
  private [this] val synchReaderId: Int = synch.linkTo(sampleRate)
  private [this] var sampleNumber: Long = 0

  private [this] var currFreq: Float = 0
  private [this] var reqFreq: Float = 0
  private [this] var origFreq: Float = 0
  private [this] var glideTime: Float = 0
  private [this] var init: Boolean = false

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
        val ev: Event = eventReader.event
        eventReader.next()

        ev.code match {
          case Event.EventCodePlayNote =>
            reqFreq = Frequency.noteIdToFrequency(ev.noteId)
            glideTime = 0
            if(!init) {
              origFreq = reqFreq
              currFreq = reqFreq
              init = true
            }
            else
              origFreq = currFreq

            howManyEventsRead += 1

          case _ =>
            glideTime = 0
            origFreq = currFreq
            howManyEventsRead += 1
        }
      }

      currFreq = glide.apply(reqFreq, origFreq, glideTime)

      bufferOut.b(ol) = currFreq
      ol = bufferOut.nextIndex(ol)
      sr = synch.nextIndex(sr)

      //yeah this is a bit rubbish, but it's not going to need the precision of long running timers
      glideTime += 1.0f / sampleRate.toFloat
      sampleNumber += 1
      i += 1
    }

    bufferOut.commitWrite(howMuch)
    eventReader.commitRead(howManyEventsRead)
    synch.commitRead(howMuch, synchReaderId)
    howMuch
  }
}
