package idiocy.music.key

/**
  * Scientific Pitch Notation Pitch
  * @param compositePitchClass
  * @param octaveNumber
  */
final case class SPNPitch(compositePitchClass: CompositePitchClass, octaveNumber: Int) {
  override def toString: String = compositePitchClass.toString + octaveNumber
}
