package idiocy.music.key

/**
  * From The Cognition of Basic Musical Structures by David Temperley.
  *
  * This doesn't actually have a name in his book beyond modified K-S, so K-S-T seems appropriate
  *
  * This class will allocate, so is not suitable for serious real time work
  */
object KruhmshalSchmucklerTemperley{
  val major: Array[Float] = Array[Float](
    5.0f, 2.0f, 3.5f, 2.0f, 4.5f, 4.0f, 2.0f, 4.5f, 2.0f, 3.5f, 1.5f, 4.0f
  )
  val minor: Array[Float] = Array[Float](
    5.0f, 2.0f, 3.5f, 4.5f, 2.0f, 4.0f, 2.0f, 4.5f, 3.5f, 2.0f, 1.5f, 4.0f
  )


  private [this] def kstScoreForKey(key: Int, scores: Array[Float], notes: Array[Int]): Float = {
    var i = 0
    var score: Float = 0
    while(i < 12){
      i += 1
      var k = i - key
      if(k < 0)
        k += 12
      score += notes(i) * scores(k)
    }
    score
  }

  /** Guess a key using the KST algorithm. Warning - allocates
    *
    * @param notes number of instances indexed by pitch class
    * @return
    */
  def kstKey(notes: Array[Int]): Key = {
    var bestScore = 0.0f
    var bestKeyPitchClass = -1
    var bestKeyMode: Scale = null

    var k = 0
    while(k < 12) {
      val majorScore = kstScoreForKey(k, major, notes)
      if(majorScore > bestScore) {
        bestScore = majorScore
        bestKeyPitchClass = k
        bestKeyMode = Scale.Major
      }

      val minorScore = kstScoreForKey(k, minor, notes)
      if(minorScore > bestScore) {
        bestScore = minorScore
        bestKeyPitchClass = k
        bestKeyMode = Scale.NaturalMinor
      }

      k += 1
    }
    ???
   //new Key(bestKeyPitchClass, bestKeyMode)
  }
}