package idiocy.music.key

final case class SPNPitch(compositePitchClass: CompositePitchClass, octaveNumber: Int) {
  override def toString: String = compositePitchClass.toString + octaveNumber
}
