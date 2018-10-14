package idiocy.music.key
import upickle.default.{macroRW, ReadWriter => RW}

object IntNaturalPitchClass{
  val C: IntNaturalPitchClass = new IntNaturalPitchClass(0)
  val D: IntNaturalPitchClass = new IntNaturalPitchClass(1)
  val E: IntNaturalPitchClass = new IntNaturalPitchClass(2)
  val F: IntNaturalPitchClass = new IntNaturalPitchClass(3)
  val G: IntNaturalPitchClass = new IntNaturalPitchClass(4)
  val A: IntNaturalPitchClass = new IntNaturalPitchClass(5)
  val B: IntNaturalPitchClass = new IntNaturalPitchClass(6)

  def apply(data: Int): IntNaturalPitchClass = new IntNaturalPitchClass(data)

  implicit def rw: RW[IntNaturalPitchClass] = macroRW
}

/**
  * This is the pitch class of the natural notes. Actually, IntPitchClass should be called Chromatic but I mean we're
  * all friends here right
  * @param data
  */
final class IntNaturalPitchClass(val data: Int) extends AnyVal {
  def toIntPitchClass: IntPitchClass = {
    data match {
      case 0 => IntPitchClass.C
      case 1 => IntPitchClass.D
      case 2 => IntPitchClass.E
      case 3 => IntPitchClass.F
      case 4 => IntPitchClass.G
      case 5 => IntPitchClass.A
      case 6 => IntPitchClass.B
    }
  }

  def next: IntNaturalPitchClass = IntNaturalPitchClass((data + 1) % 7)

  override def toString: String = data match{
    case 0 => "C"
    case 1 => "D"
    case 2 => "E"
    case 3 => "F"
    case 4 => "G"
    case 5 => "A"
    case 6 => "B"
    case _ => "?"
  }

  def +(rhs: Int): IntNaturalPitchClass = {
    IntNaturalPitchClass((data + rhs) % 7)
  }
}
