package idiocy.music.key
import upickle.default.{macroRW, ReadWriter => RW}
object PitchClassModifier{
  implicit def rw: RW[PitchClassModifier] = macroRW
  val `♮`: PitchClassModifier = PitchClassModifier(0)
  val `♭`: PitchClassModifier = PitchClassModifier(-1)
  val `𝄫`: PitchClassModifier = PitchClassModifier(-2)
  val `♯`: PitchClassModifier = PitchClassModifier(1)
  val `𝄪`: PitchClassModifier = PitchClassModifier(2)
  def apply(data: Int): PitchClassModifier = new PitchClassModifier(data)
}
final class PitchClassModifier(val data: Int) extends AnyVal {
  override def toString: String = data match {
    case -2 => "𝄫"
    case -1 => "♭"
    case 0 => ""
    case 1 => "♯"
    case 2 => "𝄪"
    case _ => "!"
  }
}
