package idiocy.music.key
import upickle.default.{macroRW, ReadWriter => RW}

object CompositePitchClass{
  implicit def rw: RW[CompositePitchClass] = macroRW
}

case class CompositePitchClass(intNaturalPitchClass: IntNaturalPitchClass,
                               pitchClassModifier: PitchClassModifier = PitchClassModifier.`♮`) {
  def toIntegerPitchClass: IntPitchClass = intNaturalPitchClass.toIntPitchClass + pitchClassModifier.data

  override def toString: String = intNaturalPitchClass.toString + pitchClassModifier.toString

  def `♮`: CompositePitchClass = CompositePitchClass(intNaturalPitchClass, PitchClassModifier.`♮`)
  def `♭`: CompositePitchClass = CompositePitchClass(intNaturalPitchClass, PitchClassModifier.`♭`)
  def `𝄫`: CompositePitchClass = CompositePitchClass(intNaturalPitchClass, PitchClassModifier.`𝄫`)
  def `♯`: CompositePitchClass = CompositePitchClass(intNaturalPitchClass, PitchClassModifier.`♯`)
  def `𝄪`: CompositePitchClass = CompositePitchClass(intNaturalPitchClass, PitchClassModifier.`𝄪`)
}
