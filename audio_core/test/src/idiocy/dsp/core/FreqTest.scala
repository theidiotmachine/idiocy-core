package idiocy.dsp.core
import utest._

object FreqTest extends TestSuite {
  val tests = Tests {

    'main - {
      assert(Frequency.noteNameToFrequency("A4") == 440)
      assert(Frequency.noteNameToFrequency("A♯8") == 7458.62f)
      assert(Frequency.noteNameToFrequency("B♭8") == 7458.62f)
      assert(Frequency.noteNameToNoteId("D♯2") == Frequency.noteNameToNoteId("E♭2"))
      assert(Frequency.noteNameToFrequency("G4") == Frequency.noteNameToFrequency("G3") * 2)
      assert(Frequency.noteNameToNoteId("A4") == 57)
      assert(Frequency.noteNameToNoteId("A0") == 9)
      assert(Frequency.noteNameToNoteId("C0") == 0)
      assert(Frequency.noteIdToStandardPiano(57) == 49)

      assert(Frequency.frequencyToNoteId(440) == Frequency.noteNameToNoteId("A4"))
      assert(Frequency.frequencyToNoteId(441) == Frequency.noteNameToNoteId("A4"))
      assert(Frequency.frequencyToNoteId(439) == Frequency.noteNameToNoteId("A4"))

      assert(Frequency.noteIdToPitchClassId(Frequency.noteNameToNoteId("C2")) == 0)
      assert(Frequency.noteIdToPitchClassId(Frequency.noteNameToNoteId("C3")) == 0)
      assert(Frequency.noteIdToOctaveNumber(Frequency.noteNameToNoteId("C3")) == 3)
      assert(Frequency.noteIdToOctaveNumber(
        Frequency.pitchClassNameAndOctaveNumberToNoteId("C", 3)) == 3
      )
      assert(Frequency.noteIdToPitchClassId(
        Frequency.pitchClassNameAndOctaveNumberToNoteId("C", 3)) == 0
      )

      assert(Frequency.noteIdToName(Frequency.midiToNoteId(60)) == "C4")
    }
  }
}
