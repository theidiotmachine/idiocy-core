package idiocy.ui.renderer

import idiocy.dsp.core.Frequency
import idiocy.music.key.{Key, IntPitchClass, Scale}

import scala.collection.mutable.ArrayBuffer

object Clef {
  val pitchClassIdNext = Array(
    2,  //C
    -1, //C#
    4,  //D
    -1, //D#
    5,  //E
    7,  //F
    -1, //F#
    9,  //G
    -1, //G#
    11, //A
    -1, //A#
    0   //B
  )

  val pitchClassIdPrev = Array(
    11, //C
    -1, //C#
    0,  //D
    -1, //D#
    2,  //E
    4,  //F
    -1, //F#
    5,  //G
    -1, //G#
    7,  //A
    -1, //A#
    9   //B
  )
}

//case class Slot(line: Boolean, noteId: Int, visible: Boolean)

case class ClefLocation(barLocation: Int, modifer: Int)

trait Clef{
  def getBarLocation(key: Key, scaleNumber: Int, noteIdModifier: Int, octaveNumber: Int): ClefLocation

  /*
  def defineSlots: Array[Slot] = {
    val zeroPitchClassId = Frequency.noteIdToPitchClassId(zeroNoteId)
    val zeroOctaveNumber = Frequency.noteIdToOctaveNumber(zeroNoteId)

    var line = true
    var pitchClassId = zeroPitchClassId
    var octaveNumber = zeroOctaveNumber
    var out: ArrayBuffer[Slot] = ArrayBuffer()
    var noteId = Frequency.pitchClassIdAndOctaveNumberToNoteId(pitchClassId, octaveNumber)

    var i = 0
    while(i < 9){
      out += Slot(line, noteId, visible = true)

      pitchClassId = Clef.pitchClassIdNext(pitchClassId)
      if(pitchClassId == 0)
        octaveNumber += 1
      noteId = Frequency.pitchClassIdAndOctaveNumberToNoteId(pitchClassId, octaveNumber)
      line = !line
      i += 1
    }

    while(noteId <= maxNoteId){
      out += Slot(line, noteId, visible = false)
      pitchClassId = Clef.pitchClassIdNext(pitchClassId)
      if(pitchClassId == 0)
        octaveNumber += 1
      noteId = Frequency.pitchClassIdAndOctaveNumberToNoteId(pitchClassId, octaveNumber)
      line = !line
    }

    pitchClassId = Clef.pitchClassIdPrev(zeroPitchClassId)
    octaveNumber = if(pitchClassId == 11) zeroOctaveNumber - 1 else zeroOctaveNumber
    line = false

    while(noteId >= minNoteId){
      out.insert(0, Slot(line, noteId, visible = false))
      pitchClassId = Clef.pitchClassIdPrev(zeroPitchClassId)
      octaveNumber = if(pitchClassId == 11) zeroOctaveNumber - 1 else zeroOctaveNumber
      noteId = Frequency.pitchClassIdAndOctaveNumberToNoteId(pitchClassId, octaveNumber)
      line = !line
    }

    out.toArray
  }

  val slots: Array[Slot] = defineSlots

  def clefAllows(noteId: Int): Boolean = noteId < maxNoteId && noteId > minNoteId
  def noteIdToBarIndex(noteId: Int): Int = {
    if(noteId > maxNoteId || noteId < minNoteId)
      throw new IllegalArgumentException("cleff does not allow")
    else {

    }
  }
  */

}

/**
  *  - a5
  *    g5
  * -- f5
  *    e5
  * -- d5
  *    c5
  * -- b4
  *    a4
  * -- g4
  *    f4
  * -- e4
  *    d4
  *  - c4
  */

object TrebleClef extends Clef(//"𝄞"//,
//  Frequency.noteNameToNoteId("C4"),
//  Frequency.noteNameToNoteId("A5"),
//  Frequency.noteNameToNoteId("E4")
){
  override def getBarLocation(key: Key, scaleNumber: Int, noteIdModifier: Int, octaveNumber: Int): ClefLocation = ???
}

object GrandClef extends Clef(//"𝄞"//,
//  Frequency.noteNameToNoteId("C4"),
//  Frequency.noteNameToNoteId("A5"),
//  Frequency.noteNameToNoteId("E4")
){
  override def getBarLocation(key: Key, scaleNumber: Int, noteIdModifier: Int, octaveNumber: Int): ClefLocation = {
    /*
    key.pitchClass match{
      case IntPitchClass.C =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 32, noteIdModifier)
      case IntPitchClass.`C♯` =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 32, noteIdModifier)
      case IntPitchClass.D =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 31, noteIdModifier)
      case IntPitchClass.`D♯` =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 31, noteIdModifier)
      case IntPitchClass.E =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 30, noteIdModifier)
      case IntPitchClass.F =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 29, noteIdModifier)
      case IntPitchClass.`F♯` =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 29, noteIdModifier)
      case IntPitchClass.G =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 28, noteIdModifier)
      case IntPitchClass.`G♯` =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 28, noteIdModifier)
      case IntPitchClass.A =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 27, noteIdModifier)
      case IntPitchClass.`A♯` =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 27, noteIdModifier)
      case IntPitchClass.B =>
        ClefLocation(scaleNumber + (octaveNumber * 8) - 26, noteIdModifier)
    }
    */
    ???
  }
}
object BassClef extends Clef(//"𝄢"//,
//  Frequency.noteNameToNoteId("E2"),
//  Frequency.noteNameToNoteId("B3"),
//  Frequency.noteNameToNoteId("G2")
){
  override def getBarLocation(key: Key, scaleNumber: Int, noteIdModifier: Int, octaveNumber: Int): ClefLocation = ???
}
object AltoClef extends Clef(//"𝄡"//,
//  Frequency.noteNameToNoteId("C2"),
//  Frequency.noteNameToNoteId("B3"),
//  Frequency.noteNameToNoteId("F3")
){
  override def getBarLocation(key: Key, scaleNumber: Int, noteIdModifier: Int, octaveNumber: Int): ClefLocation = ???
}
