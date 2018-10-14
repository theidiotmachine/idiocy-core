package idiocy.dsp.pitch

/**
  * This is from Tarsos, but it's my less clean but slightly more deterministic API. We expect users to fill in the
  * window, calc the pitch, look at the output, repeat. The advantage of this is that the window is fixed, so if the mic
  * is going very fast and spitting out tiny chunks, we won't process them until the window is full. The disadvantage
  * is that it goes against every rule of writing modern clean code - this is a very old-fashioned way of doing it!
  * You can write Fortran in any language and Scala is no exception...
  */
trait PitchDetector {
  /**
    * The inputs
    */
  val buffer: Array[Float]
  val sz: Int

  /**
    * The outputs
    */
  //The pitch in Hertz.
  var pitch: Float
  //A probability (noisiness, (a)periodicity, salience, voicedness or
  //clarity measure) for the detected pitch. This is somewhat similar
  //to the term voiced which is used in speech recognition. This
  //probability should be calculated together with the pitch. The
  //exact meaning of the value depends on the detector used.
  var probability: Float
  //Whether the algorithm thinks the block of audio is pitched. Keep
  // in mind that an algorithm can come up with a best guess for a
  //pitch even when isPitched() is false
  var pitched: Boolean

  /**
    * Calculate the outputs for a given window. Obviously everything is mutating while this is called
    */
  def calcPitch(): Unit
}
