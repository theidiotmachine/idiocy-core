package idiocy.music.key
import upickle.default.{ReadWriter => RW, macroRW}

object IntPitchClass {
  val C = IntPitchClass(0)
  val `C♯` = IntPitchClass(1)
  val `D♭` = IntPitchClass(1)
  val D = IntPitchClass(2)
  val `D♯` = IntPitchClass(3)
  val `E♭` = IntPitchClass(3)
  val E = IntPitchClass(4)
  val F = IntPitchClass(5)
  val `F♯` = IntPitchClass(6)
  val `G♭` = IntPitchClass(6)
  val G = IntPitchClass(7)
  val `G♯` = IntPitchClass(8)
  val `A♭` = IntPitchClass(8)
  val A = IntPitchClass(9)
  val `A♯` = IntPitchClass(10)
  val `B♭` = IntPitchClass(10)
  val B = IntPitchClass(11)

  val MaxIntegerPitchClass = 11

  def apply(data: Int): IntPitchClass = {
    new IntPitchClass(data % 12)
  }
  implicit def rw: RW[IntPitchClass] = macroRW
}

class IntPitchClass(val data: Int) extends AnyVal{
  def +(rhs: Int): IntPitchClass = new IntPitchClass((data + rhs) % 12)
}
