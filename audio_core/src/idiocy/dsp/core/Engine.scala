package idiocy.dsp.core

import idiocy.dsp.ugens.UGen

class Engine(val bufferTimeSeconds: Float, val defaultSampleRate: Int) {
  private [this] val bufferTimeMillis: Long = (bufferTimeSeconds * 1000).toLong
  def runOnce(): Unit = {
    var i = 0
    var howMuch = 0
    while(i < uGens.length){
      howMuch += uGens(i).run()
      i += 1
    }
    if(howMuch == 0) {
      //if there has been no output at all, we may be blocked on io, so don't sleep too long. Of course this could
      //lead to death spirals
      Thread.sleep(1)
    }
  }

  def sz(sampleRate: Int): Int = if(sampleRate == 0)
    (bufferTimeSeconds * defaultSampleRate).toInt
  else
    (bufferTimeSeconds * sampleRate).toInt

  private var uGens: Array[UGen] = Array()

  def register(uGen: UGen): Unit = {
    uGens = uGens :+ uGen
  }

  private [this] def deadBranchCheck(): Unit = {
    var i = 0
    while(i < uGens.length){
      val uGen = uGens(i)
      val signalOuts = uGen.signalOuts
      var j = 0
      while(j < signalOuts.length){
        if(signalOuts(j).numReaders == 0)
          throw new IllegalStateException("dead branch found")
        j += 1
      }
      i += 1
    }
  }

  def run(): Unit = {
    deadBranchCheck()
    val start = System.currentTimeMillis()
    //var first = false
    while( true ||
      System.currentTimeMillis() < start + 10000) {
      runOnce()
      //if(!first)
        //blockageCheck()
      //first = false
    }
  }
}
