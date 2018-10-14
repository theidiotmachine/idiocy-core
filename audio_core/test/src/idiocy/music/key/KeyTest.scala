package idiocy.music.key

import utest._

object KeyTest extends TestSuite{
  val tests = Tests{
    'dmaj - {
      val dMaj = Key(new CompositePitchClass(IntNaturalPitchClass.D), Scale.Major)
      assert(dMaj.intPitchClasses sameElements
        Array(IntPitchClass.D, IntPitchClass.E, IntPitchClass.`F♯`, IntPitchClass.G, IntPitchClass.A, IntPitchClass.B, IntPitchClass.`C♯`))


      assert(dMaj.compositePitchClasses sameElements Array(
        CompositePitchClass(IntNaturalPitchClass.D),
        CompositePitchClass(IntNaturalPitchClass.E),
        CompositePitchClass(IntNaturalPitchClass.F, PitchClassModifier.`♯`),
        CompositePitchClass(IntNaturalPitchClass.G),
        CompositePitchClass(IntNaturalPitchClass.A),
        CompositePitchClass(IntNaturalPitchClass.B),
        CompositePitchClass(IntNaturalPitchClass.C, PitchClassModifier.`♯`),
      ))
    }
  }
}
