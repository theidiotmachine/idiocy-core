package idiocy.music.key
import upickle.default.{macroRW, ReadWriter => RW}

case class Key(pitchClass: CompositePitchClass, scale: Scale) {
  def asTraditionalKey: Key = new Key(pitchClass, scale.asTraditionalKeySignatureScale)

  /**
    * Returns the set of pitchclasses in this key, starting at the tonic. So for example
    * D Major would be
    * {{{
    * Array(IntPitchClass.D, IntPitchClass.E, IntPitchClass.`F♯`,
    *   IntPitchClass.G, IntPitchClass.A, IntPitchClass.B, IntPitchClass.`C♯`)
    * }}}
    *
    * @return
    */

  val intPitchClasses: Array[IntPitchClass] = {
    val out = new Array[IntPitchClass](scale.notes.length)
    var i = 0
    while(i < scale.notes.length){
      out(i) = pitchClass.toIntegerPitchClass + scale.notes(i)
      i += 1
    }
    out
  }

  /**
    * Returns the set of pitchclasses in this key starting at the tonic. So for example, D Maj would be
    * {{{
    * Array(
    *   CompositePitchClass(IntNaturalPitchClass.D, PitchClassModifier.`♮`)
    *   CompositePitchClass(IntNaturalPitchClass.E, PitchClassModifier.`♮`),
    *   CompositePitchClass(IntNaturalPitchClass.F, PitchClassModifier.`♯`),
    *   CompositePitchClass(IntNaturalPitchClass.G, PitchClassModifier.`♮`),
    *   CompositePitchClass(IntNaturalPitchClass.A, PitchClassModifier.`♮`),
    *   CompositePitchClass(IntNaturalPitchClass.B, PitchClassModifier.`♮`),
    *   CompositePitchClass(IntNaturalPitchClass.C, PitchClassModifier.`♯`),
    * )
    * }}}
    */
  val compositePitchClasses: Array[CompositePitchClass] = {
    var naturalBase = pitchClass.intNaturalPitchClass
    var i = 0
    val out = new Array[CompositePitchClass](intPitchClasses.length)
    while(i < intPitchClasses.length){
      val ipc = intPitchClasses(i)
      val mod = PitchClassModifier(ipc.data - naturalBase.toIntPitchClass.data)
      out(i) = new CompositePitchClass(naturalBase, mod)

      naturalBase = naturalBase.next
      i += 1
    }
    out
  }
}

object Key{
  implicit def rw: RW[Key] = macroRW
  val CMajor = Key(CompositePitchClass(IntNaturalPitchClass.C, PitchClassModifier.`♮`), Scale.Major)
}