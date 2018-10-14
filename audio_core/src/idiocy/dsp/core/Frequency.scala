package idiocy.dsp.core

import scala.collection.mutable

object Frequency {

  val maxNoteIdentity = 107
  private [this] val a4NoteFreq = 440.0
  private [this] val a4NoteId = 57

  /**
    * Both zero based
    * @param id
    * @return
    */
  def noteIdToStandardPiano(id: Int): Int = id - 8

  /**
    * Both zero based
    * @param piano
    * @return
    */
  def standardPianoToNoteIdentity(piano: Int): Int = piano + 8

  def noteIdToMidi(id: Int): Int = id + 12

  def midiToNoteId(midi: Int): Int = midi - 12

  /**
    * Note name constants. Oh scala unicode how I love thee
    */
  val pitchClassOrder = Array("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
  val pitchClassEquivalences = Map(
    "C♯" -> "D♭",
    "D♯" -> "E♭",
    "F♯" -> "G♭",
    "G♯" -> "A♭",
    "A♯" -> "B♭"
  )

  val noteIdToName: Array[String] = new Array(maxNoteIdentity+1)
  val noteIdToFrequency: Array[Float] = new Array(maxNoteIdentity+1)
  val noteNameToFrequency: mutable.Map[String, Float] = mutable.Map()
  val noteNameToNoteId: mutable.Map[String, Int] = mutable.Map()

  def pitchClassNameAndOctaveNumberToNoteId(pitchClass: String, octave: Int): Int = noteNameToNoteId(pitchClass + octave)

  /**
    * wikipedia calls these things pitch classes, but also mentions 'chroma' which is lovely.
    * @param noteId note id
    * @return the pitch class; c = 0, wraps at b
    */
  def noteIdToPitchClassId(noteId: Int): Int = {
    noteId % 12
  }

  def noteIdToOctaveNumber(noteId: Int): Int = {
    noteId / 12
  }

  def pitchClassIdAndOctaveNumberToNoteId(pitchClassId: Int, octaveNumber: Int): Int = {
    octaveNumber * 12 + pitchClassId
  }

  /**
    * Guess a note from a frequency. Because it's using linear difference, it's not going to be very good at getting
    * smooth glides; you have to make a vague attempt to hit the note
    * @param f frequency
    * @return the noteId
    */
  def frequencyToNoteId(f: Float): Int = {
    val numNotes = noteIdToFrequency.length
    var left: Int = 0
    var right: Int = numNotes - 1
    var searching = true
    var out = -1
    while (searching && right > left) {
      val mid = left + (right - left) / 2
      val thisF = noteIdToFrequency(mid)
      val diff = thisF - f
      if(math.abs(diff) < 1e-2){
        searching = false
        out = mid
      }
      else if (diff > 0)
        right = mid - 1
      else if (diff < 0)
        left = mid + 1
    }
    if(searching) {
      //left == right, so it's one of l-1, l, l+1
      if(left==0 || left == numNotes - 1) {
        -1 //very high or low means we can't accurately guess, so bail
      } else {
        val lf = noteIdToFrequency(left)
        val lf1 = noteIdToFrequency(left+1)

        if(f > lf) {
          if(math.abs(lf - f) < math.abs(lf1 - f))
            left
          else
            left + 1
        } else {
          val lfn1 = noteIdToFrequency(left - 1)
          if(math.abs(lf - f) < math.abs(lfn1 - f))
            left
          else
            left - 1
        }
      }
    } else
      out
  }

  {
    val a = math.pow(2, 1.0/12.0)
    var n = 0
    var noteNameIndex = 0
    var octaveNumber = 0
    while(n<=maxNoteIdentity) {
      val freq = (a4NoteFreq * math.pow(a, n-a4NoteId)).toFloat
      noteIdToFrequency(n) = freq
      val sharpName = pitchClassOrder(noteNameIndex)+octaveNumber
      noteIdToName(n) = sharpName
      noteNameToFrequency(sharpName) = freq
      noteNameToNoteId(sharpName) = n
      val oFlatName = pitchClassEquivalences.get(pitchClassOrder(noteNameIndex)).flatMap(s=>Some(s+octaveNumber))
      oFlatName.foreach(flatName=>{
        noteNameToFrequency(flatName) = freq
        noteNameToNoteId(flatName) = n
      })
      noteNameIndex += 1
      if(noteNameIndex==pitchClassOrder.length) {
        noteNameIndex = 0
        octaveNumber += 1
      }
      n+=1
    }
  }
}
