package idiocy.music.key

object Dynamics {
  //pianississimo
  val ppp = 0
  //pianissimo
  val pp = 1
  //piano
  val p = 2
  //mezzo-piano
  val mp = 3
  //mezzo-forte
  val mf = 4
  //forte
  val f = 5
  //fortissimo
  val ff = 6
  //fortississimo
  val fff = 7

  val `𝆏𝆏𝆏`: Int = ppp
  val `𝆏𝆏`: Int = pp
  val `𝆏`: Int = p
  val `𝆐𝆏`: Int = mp
  val `𝆐𝆑`: Int = mf
  val `𝆑`: Int = f
  val `𝆑𝆑`: Int = ff
  val `𝆑𝆑𝆑`: Int = fff

  val numDynamics = 8

  val dynamicsToVelocity: Array[Float] = Array(
    0.125f,
    0.25f,
    0.375f,
    0.5f,
    0.625f,
    0.75f,
    0.875f,
    1
  )
}
