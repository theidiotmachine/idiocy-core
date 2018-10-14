package idiocy.music.key

import idiocy.dsp.core.Frequency
import upickle.default.{ReadWriter => RW, macroRW}

object Scale {
  implicit def rw: RW[Scale] = macroRW

  val ScaleTypeMajor: Int = 0
  val ScaleTypeMinor: Int = 1
  val ScaleTypeMajorVariant: Int = 2
  val ScaleTypeMinorVariant: Int = 3
  val ScaleTypeMajorPentatonic: Int = 4
  val ScaleTypeMinorPentatonic: Int = 5
  val ScaleTypeOther: Int = 6

  val MajorRaw = Array(0, 2, 4, 5, 7, 9, 11)

  //@formatter:off
  val Major: Scale =                   new Scale(Array("1",  "2",  "3",  "4",  "5",  "6",  "7" ), ScaleTypeMajor)
  val NaturalMinor: Scale =            new Scale(Array("1",  "2",  "♭3", "4",  "5",  "♭6", "♭7"), ScaleTypeMinor)
  val HarmonicMinor: Scale =           new Scale(Array("1",  "2",  "♭3", "4",  "5",  "♭6", "7" ), ScaleTypeMinorVariant)
  //aka Jazz minor scale
  val AscendingMelodicMinor: Scale =   new Scale(Array("1",  "2",  "♭3", "4",  "5",  "6",  "7" ), ScaleTypeMinorVariant)
  val DescendingMelodicMinor: Scale =  NaturalMinor
  //aka Egyptian minor scale
  val HungarianMinor: Scale =          new Scale(Array("1",  "2",  "♭3", "♯4", "5",  "♭6", "7" ), ScaleTypeMinorVariant)

  val IonianMode: Scale =              Major
  val DorianMode: Scale =              new Scale(Array("1",  "2",  "♭3", "4",  "5",  "6",  "♭7"), ScaleTypeMinorVariant)
  val PhrygianMode: Scale =            new Scale(Array("1",  "♭2", "♭3", "4",  "5",  "♭6", "♭7"), ScaleTypeMinorVariant)
  //aka Spanish gypsy scale
  val PhrygianDominantMode: Scale =    new Scale(Array("1",  "♭2", "3",  "4",  "5",  "♭6", "♭7"), ScaleTypeMajorVariant)
  val LydianMode: Scale =              new Scale(Array("1",  "2",  "3",  "♯4", "5",  "6",  "7" ), ScaleTypeMajorVariant)
  //aka Moloch = C, F, G7 (Gm)
  val MixolydianMode: Scale =          new Scale(Array("1",  "2",  "3",  "4",  "5",  "6",  "♭7"), ScaleTypeMajorVariant)
  val AeolianMode: Scale =             NaturalMinor
  //'very rarely used' - wiki
  val LocrianMode: Scale =             new Scale(Array("1",  "♭2", "♭3", "4",  "♭5", "♭6", "♭7"), ScaleTypeMinorVariant)

  val AscendingDominantBebop: Scale =  new Scale(Array("1",  "2",  "3",  "4",  "5",  "6",  "♭7", "7" ), ScaleTypeOther)
  val DescendingDominantBebop: Scale = new Scale(Array("1",  "2",  "3",  "4",  "5",  "♭6", "6",  "♭7"), ScaleTypeOther)
  val MajorBebop: Scale =              new Scale(Array("1",  "2",  "3",  "4",  "5",  "♯5", "6",  "7" ), ScaleTypeOther)

  //aka Phyyrigian ♮6, Dorian ♭2, or Phrygidorian
  val AssyrianMode: Scale =            new Scale(Array("1", "♭2", "♭3", "4",  "5",   "6",  "♭7"), ScaleTypeMinorVariant)
  //aka Lydian ♯5
  val LydianAugmentedMode: Scale =     new Scale(Array("1",  "2",  "3", "♯4", "♯5",  "6",  "7" ), ScaleTypeMajorVariant)
  //aka Lydian ♭7, Acoustic, Mixolydian ♯4
  val LydianDominantMode: Scale =      new Scale(Array("1",  "2",  "3", "♯4",  "5",  "6",  "♭7"), ScaleTypeMajorVariant)
  //aka Mixolydian ♭6, melodic major, fifth mode of melodic minor, Hindu
  //name chosen from set because it was cool
  val Myxaeolian: Scale =              new Scale(Array("1",  "2",  "3",  "4",  "5",  "♭6", "♭7"), ScaleTypeMajorVariant)
  //aka Locrian ♮2, half-diminished
  val Aeolocrian: Scale =              new Scale(Array("1",  "2",  "♭3", "4",  "♭5", "♭6", "♭7"), ScaleTypeMinorVariant)
  //aka altered dominant scale or super Locrian
  val AlteredScale: Scale =            new Scale(Array("1",  "♭2", "♭3", "♭4", "♭5", "♭6", "♭7"), ScaleTypeMinorVariant)

  //aka Octatonic. i think this is correct
  val SymmetricHalfFirst: Scale =      new Scale(Array("1",  "♭2", "♭3", "3",  "♯4", "5",  "6",  "♭7"), ScaleTypeOther)
  val symmetricWholeFirst: Scale =     new Scale(Array("1",  "2",  "♭3", "4",  "♯4", "♯5", "6",  "7" ), ScaleTypeOther)

  val MajorPentatonic: Scale =         new Scale(Array("1",  "2",  "3",  "5",  "6" ), ScaleTypeMajorPentatonic)
  val MinorPentatonic: Scale =         new Scale(Array("1",  "♭3", "4",  "5",  "♭7"), ScaleTypeMinorPentatonic)

  //idiocy notes: no dominant tone
  val WholeTone: Scale =               new Scale(Array(0,    2,    4,    6,    8,    10), ScaleTypeOther)

  //idiocy notes: mix pentatonics with blues
  val MajorBluesAscending: Scale =     new Scale(Array("1",  "2",  "♯2", "3",  "5",  "6" ), ScaleTypeOther)
  val MajorBluesDescending: Scale =    new Scale(Array("1",  "2",  "♭3", "3",  "5",  "6" ), ScaleTypeOther)
  val MinorBlues: Scale =              new Scale(Array("1",  "♭3", "4",  "♭5", "5",  "♭7"), ScaleTypeOther)
  //@formatter:on
}

trait ScaleConverter {
  def convert(scaleNumber: Int, pitchModifier: Int): (Int, Int)
}

object ScaleConverterIdentity extends ScaleConverter {
  override def convert(scaleNumber: Int, pitchModifier: Int): (Int, Int) = (scaleNumber, pitchModifier)
}

case class ScaleConverter7To7(from: Scale, to: Scale) extends ScaleConverter{
  override def convert(scaleNumber: Int, pitchModifier: Int): (Int, Int) = {
    val f = from.notes(scaleNumber)
    val t = to.notes(scaleNumber)
    if(f == t){
      (scaleNumber, 0)
    } else {
      (scaleNumber, t-f)
    }
  }
}

case class Scale(notes: Array[Int], scaleType: Int) {
  def asTraditionalKeySignatureScale: Scale = scaleType match {
    case Scale.ScaleTypeMajor => Scale.Major
    case Scale.ScaleTypeMinor => Scale.NaturalMinor
    case _ => ???
  }

  def getConverter(to: Scale): ScaleConverter = {
    scaleType match {
      case Scale.ScaleTypeMajor => to.scaleType match{
        case Scale.ScaleTypeMajor => ScaleConverterIdentity
        case Scale.ScaleTypeMinor => ScaleConverter7To7(this, to)
      }
      case Scale.ScaleTypeMinor => to.scaleType match {
        case Scale.ScaleTypeMajor => ScaleConverter7To7(this, to)
        case Scale.ScaleTypeMinor => ScaleConverterIdentity
      }
      case Scale.ScaleTypeMajorVariant => to.scaleType match {
        case Scale.ScaleTypeMajor => ScaleConverter7To7(this, to)
        case Scale.ScaleTypeMinor => ScaleConverter7To7(this, to)
      }
      case Scale.ScaleTypeMinorVariant => to.scaleType match {
        case Scale.ScaleTypeMajor => ScaleConverter7To7(this, to)
        case Scale.ScaleTypeMinor => ScaleConverter7To7(this, to)
      }
    }
  }

  def this(in: Array[String], scaleType: Int){
    this(in.map(s=>{
      if(s.startsWith("♯")){
        val frag = s.substring(1, 2)
        Scale.MajorRaw(frag.toInt - 1) + 1
      } else if(s.startsWith("♭")){
        val frag = s.substring(1, 2)
        Scale.MajorRaw(frag.toInt - 1) - 1
      } else {
        Scale.MajorRaw(s.toInt - 1)
      }
    }), scaleType)
  }

  /**
    * Get a noteid. So for example if you were playing the fifth in A Major, and you had decided the root was A3, then
    * you would pass scaleNumber = 5, pitchClass = PitchClass.A, octaveNumber = 3
    * @param scaleNumber the index in the scale
    * @param pitchClass pitch class of the tonic of the scale
    * @param octaveNumber octave number of the tonic of the scale. Yeah I know
    * @return
    */
  //not used?
  def noteId(scaleNumber: Int, pitchClass: IntPitchClass, octaveNumber: Int): Int = {
    if(scaleNumber >= 0) {
      val thisScaleNumber = scaleNumber % 8
      Frequency.pitchClassIdAndOctaveNumberToNoteId(pitchClass.data, octaveNumber) + notes(thisScaleNumber)
    } else
      ???
  }
}
