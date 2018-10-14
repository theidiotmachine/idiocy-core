package idiocy.dsp.ugens


import idiocy.dsp.core.{Engine, SignalBufferIn}
import idiocy.dsp.filter.{Filter, LowPassBiQuadFilter}
import idiocy.dsp.ugens.Reverb.ReverbUnit

object Reverb{
  class ReverbUnit(val delaySamples: Int,
                   val gain: Float,
                   val filterGain: Float,
                   val filter: Filter) {

    def init(sampleRate: Int): Unit = {
      filter.calcCoeff()
    }
  }

  def createSimple(e: Engine,
                   in: SignalBufferIn,
                   feedbackDryGain: Float,
                   feedbackWetGain: Float,
                   numUnits: Int,
                   spread: Int,
                   unitWetGain: Float,
                   unitWetGainDecay: Float,
                   unitFilterGain: Float,
                   unitFilterGainDecay: Float,
                   unitFilterFreq: Float,
                   unitFilterFreqDecay: Float,
                   unitFilterQ: Float,
                   unitFilterQDecay: Float
                  ): Reverb = {
    val units = new Array[ReverbUnit](numUnits)
    var i = 0
    var pid = 0
    var netGain = feedbackDryGain
    var thisUnitWetGain = unitWetGain
    var thisUnitFilterGain = unitFilterGain
    var thisUnitFilterFreq = unitFilterFreq
    var thisUnitFilterQ = unitFilterQ
    while(i < numUnits){
      netGain += feedbackWetGain * (thisUnitWetGain + thisUnitFilterGain)
      if(netGain > 2.0f)
        throw new IllegalArgumentException("Probably feedback")
      units(i) = new ReverbUnit(primes(pid), thisUnitWetGain, thisUnitFilterGain,
        new LowPassBiQuadFilter(in.sampleRate, thisUnitFilterFreq, thisUnitFilterQ))
      thisUnitWetGain *= unitWetGainDecay
      thisUnitFilterGain *= unitFilterGainDecay
      thisUnitFilterFreq *= unitFilterFreqDecay
      thisUnitFilterQ *= unitFilterQDecay
      pid += spread
      i += 1
    }
    new Reverb(e, in, feedbackDryGain, units, feedbackWetGain = feedbackWetGain)
  }

  val primes = Array(
    2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,
    107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211,
    223,227,229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,331,
    337,347,349,353,367,379,389,401,419,431,439,449,461,467,487,499,509,523,547,563,
    571,587,599,607,617,631,643,653,661,677,691,709,727,739,751,761,773,797,811,823,
    829,853,859,877,883,911,937,953,977,997,1019,1033,1051,1069,1093,1109,1129,1163,
    1187,1213,1229,1249,1279,1291,1303,1321,1367,1399,1427,1439,1453,1481,1489,1511,
    1549,1571,1601,1619,1657,1693,1721,1747,1783,1811,1861,1877,1907,1949,1987,2003,
    2029,2069,2089,2129,2143,2203,2239,2273,2309,2347,2381,2411,2447,2503,2549,2593,
    2647,2677,2699,2729,2767,2801,2843,2887,2927,2971,3037,3083,3163,3203,3253,3307,
    3343,3389,3457,3499,3539,3581,3623,3673,3727,3793,3851,3911,3947,4019,4079,4133,
    4211,4253,4297,4373,4447,4513,4583,4649,4721,4789,4871,4937,4993,5051,5113,5197,
    5279,5381,5437,5503,5573,5657,5737,5813,5867,5953,6053,6131,6217,6299,6361,6469,
    6569,6661,6737,6829,6911,6997,7109,7213,7321,7457,7537,7603,7699,7817,7919,8053,
    8161,8263,8369,8501,8609,8699,8807,8929,9041,9161,9281,9397,9479,9623,9743,9857,
    10007,10139,10259,10369,10513,10663,10831,10957,11113,11257,11399,11551,11731,
    11887,12011,12161,12323,12479,12611,12763,12923,13093,13241,13421,13619,13759,
    13921,14143,14347,14537,14713,14851,15061,15233,15383,15583,15761,15937,16141,
    16381,16607,16829,17027,17239,17449,17659,17903,18089,18289,18503,18773,19069,
    19309,19501,19759,19993,20219,20477,20747,21001,21227,21503,21739,22003,22259,
    22541,22783,23057,23333,23629,23893,24133,24473,24821,25147,25447,25747,26041,
    26357,26693,26959,27329,27701,27983,28351,28649,28979,29333,29683,30103,30469,
    30841,31183,31547,31991,32341,32687,33049,33457,33791,34231,34603,35051,35447,
    35911,36319,36749,37159,37571,38053,38569,38993,39419,39883,40427,40897,41357,
    41849,42283,42719,43237,43787
)
}

class Reverb(e: Engine,
             val in: SignalBufferIn,
             val feedbackDryGain: Float,
             val units: Array[ReverbUnit],
             val feedbackWetGain: Float = 1.0f,
             val clip: Boolean = true) extends UGen(e) with UGenSignalOut1 {
  initOut(e, in.sampleRate)
  units.foreach(x => x.init(in.sampleRate))
  private[this] val delaySz = units.foldLeft(0)((x, y) => {
    math.max(x, y.delaySamples)
  })

  private[this] val delayBuffer: Array[Float] = new Array(delaySz)
  private[this] var delayLoc = 0

  private[this] val readIndex: in.ReaderId = in.linkTo(in.sampleRate)

  private[this] def getHowMuch: Int = {
    math.min(bufferOut.writeCapacity, in.readCapacity(readIndex))
  }

  private[this] def prevLoc(in: Int, unit: ReverbUnit): Int = {
    val out = in - unit.delaySamples
    if (out < 0)
      out + delaySz
    else
      out
  }

  override def runInternal(): Int = {
    val howMuch = getHowMuch
    var i = 0
    var lw = bufferOut.writePtr
    var lr = in.readPtr(readIndex)
    while (i < howMuch) {
      var j = 0
      var o: Float = 0 //in.b(lr) * dryGain
      while (j < units.length) {
        val unit = units(j)
        val delayValue = delayBuffer(prevLoc(delayLoc, unit))
        o += delayValue * unit.gain + unit.filter.apply(delayValue) * unit.filterGain
        j += 1
      }

      if (clip) {
        if (o > 1)
          o = 1
        if (o < -1)
          o = -1
      }

      bufferOut.b(lw) = o

      //if(!clip){
      if (o > 1)
        o = 1
      if (o < -1)
        o = -1
      //}
      delayBuffer(delayLoc) = o * feedbackWetGain + in.b(lr) * feedbackDryGain
      delayLoc += 1
      if (delayLoc == delaySz)
        delayLoc = 0
      lr = in.nextIndex(lr)
      lw = bufferOut.nextIndex(lw)
      i += 1
    }
    bufferOut.commitWrite(howMuch)
    in.commitRead(howMuch, readIndex)
    howMuch
  }
}