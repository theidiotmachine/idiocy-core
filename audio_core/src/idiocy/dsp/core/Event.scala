package idiocy.dsp.core

final class Event(var sampleNumber: Long, var code: Int, var f0: Float, var f1: Float, var i0: Int) {
  def copyFrom(event: Event): Unit = {
    sampleNumber = event.sampleNumber
    code = event.code
    f0 = event.f0
    f1 = event.f1
    i0 = event.i0
  }

  def playNote(t: Long, noteId: Int, velocity: Float, volume: Float): Unit = {
    sampleNumber = t
    code = Event.EventCodePlayNote
    f0 = velocity
    f1 = volume
    i0 = noteId
  }

  def noteId: Int = i0
  def velocity: Float = f0
  def volume: Float = f1

  def stopNote(t: Long, noteId: Int, release: Float): Unit = {
    sampleNumber = t
    code = Event.EventCodeStopNote
    f0 = release
    i0 = noteId
  }

  def release: Float = f0

  def playSample(t: Long, sampleId: Int): Unit = {
    sampleNumber = t
    code = Event.EventCodePlaySample
    i0 = sampleId
  }

  def stopSample(t: Long): Unit = {
    sampleNumber = t
    code = Event.EventCodeStopSample
  }

  def sampleId: Int = i0
}

object Event {
  //codes
  val EventCodePlayNote: Int = 1
  val EventCodeStopNote: Int = 2
  val EventCodePlaySample: Int = 3
  val EventCodeStopSample: Int = 4
}