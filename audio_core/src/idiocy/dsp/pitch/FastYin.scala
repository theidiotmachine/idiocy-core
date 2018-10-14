/*
* This is from Tarsos. Original file header follows
*
*      _______                       _____   _____ _____
*     |__   __|                     |  __ \ / ____|  __ \
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
*
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
*
*
*  I took Joren's Code and changed it so that
*  it uses the FloatFFT to calculate the difference function.
*  TarsosDSP is developed by Joren Six at
*  The Royal Academy of Fine Arts & Royal Conservatory,
*  University College Ghent,
*  Hoogpoort 64, 9000 Ghent - Belgium
*
*  http://tarsos.0110.be/tag/TarsosDSP
*
*/

/**
  * This was ported from the tarsos code to scala, and I used a different FFT implementation (I didn't much like the one
  * in Tarsos). All the bugs are mine.
  */

package idiocy.dsp.pitch

import idiocy.dsp.fft.FloatFFT

/**
  * An implementation of the YIN pitch tracking algorithm which uses an FloatFFT to
  * calculate the difference function. This makes calculating the difference
  * function more performant. See <a href=
  * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
  * >the YIN paper.</a> This implementation is done by <a href="mailto:matthias.mauch@elec.qmul.ac.uk">Matthias Mauch</a> and is
  * based on the Tarsos Yin which is based on the implementation found in <a
  * href="http://aubio.org">aubio</a> by Paul Brossier.
  *
  * @author Matthias Mauch
  * @author Joren Six
  * @author Paul Brossier
  * @param sz           The size of a buffer. E.g. 1024.
  * @param sampleRate   Sample rate
  * @param yinThreshold The parameter that defines which peaks are kept as possible
  *                     pitch candidates. See the YIN paper for more details.
  */
class FastYin(val sz: Int, val sampleRate: Int, val yinThreshold: Double = 0.20f) extends PitchDetector{
  private [this] val yinBuffer: Array[Float] = new Array[Float](sz/2)
  private [this] val powerTerms: Array[Float] = new Array[Float](sz/2)

  /**
    * Holds the FFT data, twice the length of the audio buffer.
    */
  private [this] val audioBufferFFTReal = new Array[Float](sz)
  private [this] val audioBufferFFTImaginary: Array[Float] = new Array[Float](sz)
  private [this] val fft: FloatFFT = new FloatFFT(sz)

  /**
    * Half of the data, disguised as a convolution kernel.
    */
  private [this] val kernelReal = new Array[Float](sz)
  private [this] val kernelImaginary: Array[Float] = new Array[Float](sz)

  /**
    * Buffer to allow convolution via complex multiplication. It calculates the auto correlation function (ACF).
    */
  private [this] val yinStyleACFReal = new Array[Float](sz)
  private [this] val yinStyleACFImaginary = new Array[Float](sz)

  /**
    * The inputs
    */
  val buffer: Array[Float] = new Array(sz)

  /**
    * The outputs
    */
  override var pitch: Float = -1
  override var probability: Float = 0
  override var pitched: Boolean = false

  /**
    * Implements the difference function as described in step 2 of the YIN
    * paper with an FloatFFT to reduce the number of operations.
    */
  private [this] def difference(): Unit = {
    // POWER TERM CALCULATION
    // ... for the power terms in equation (7) in the Yin paper
    var j = 0
    while (j < powerTerms.length) {
      powerTerms(j) = 0
      j += 1
    }
    j = 0
    while (j < yinBuffer.length) {
      powerTerms(0) += buffer(j) * buffer(j)
      j += 1
    }
    // now iteratively calculate all others (saves a few multiplications)
    var tau = 1
    while (tau < yinBuffer.length) {
      powerTerms(tau) = powerTerms(tau - 1) - buffer(tau - 1) * buffer(tau - 1) + buffer(tau + yinBuffer.length) * buffer(tau + yinBuffer.length)
      tau += 1
    }
    // YIN-STYLE AUTOCORRELATION via FloatFFT
    // 1. data
    j = 0
    while (j < buffer.length) {
      audioBufferFFTReal(j) = buffer(j)
      audioBufferFFTImaginary(j) = 0
      /*
      audioBufferFFT(2 * j) = buffer(j)
      audioBufferFFT(2 * j + 1) = 0
      */
      j += 1
    }
    //fft.complexForward(audioBufferFFT)
    fft.transform(audioBufferFFTReal, audioBufferFFTImaginary)

    // 2. half of the data, disguised as a convolution kernel
    j = 0
    while (j < yinBuffer.length) {
      //kernel(2 * j) = audioBuffer((yinBuffer.length - 1) - j)
      kernelReal(j) = buffer((yinBuffer.length - 1) - j)
      //kernel(2 * j + 1) = 0
      kernelImaginary(j) = 0
      //kernel(2 * j + audioBuffer.length) = 0
      kernelReal(j + buffer.length/2) = 0
      //kernel(2 * j + audioBuffer.length + 1) = 0
      kernelImaginary(j + buffer.length/2) = 0

      j += 1
    }
    //fft.complexForward(kernel)
    fft.transform(kernelReal, kernelImaginary)

    // 3. convolution via complex multiplication
    j = 0
    while (j < buffer.length) {
      //yinStyleACF(2 * j) = audioBufferFFT(2 * j) * kernel(2 * j) - audioBufferFFT(2 * j + 1) * kernel(2 * j + 1) // real
      yinStyleACFReal(j) = audioBufferFFTReal(j) * kernelReal(j) - audioBufferFFTImaginary(j) * kernelImaginary(j) // real

      //yinStyleACF(2 * j + 1) = audioBufferFFT(2 * j + 1) * kernel(2 * j) + audioBufferFFT(2 * j) * kernel(2 * j + 1) // imaginary
      yinStyleACFImaginary(j) = audioBufferFFTImaginary(j) * kernelReal(j) + audioBufferFFTReal(j) * kernelImaginary(j) // imaginary

      j += 1
    }
    fft.inverseTransform(yinStyleACFReal, yinStyleACFImaginary)
    //*_* we do the scale here. I am not using the convolve because it allocates. Perhaps I should.
    j = 0
    val scale = 1.0f/ yinStyleACFReal.length.toFloat
    while(j < yinStyleACFReal.length){
      yinStyleACFReal(j) *= scale
      yinStyleACFImaginary(j) *= scale //actually don't need to do that
      j += 1
    }

    // CALCULATION OF difference function
    // ... according to (7) in the Yin paper.
    j = 0
    while (j < yinBuffer.length) { // taking only the real part
      //yinBuffer(j) = powerTerms(0) + powerTerms(j) - 2 * yinStyleACF(2 * (yinBuffer.length - 1 + j))
      yinBuffer(j) = powerTerms(0) + powerTerms(j) - 2 * yinStyleACFReal(yinBuffer.length - 1 + j)
      j += 1
    }
  }

  /**
    * The cumulative mean normalized difference function as described in step 3
    * of the YIN paper. <br>
    * <code>
    * yinBuffer[0] == yinBuffer[1] = 1
    * </code>
    */
  private [this] def cumulativeMeanNormalizedDifference(): Unit = {
    var tau = 0
    yinBuffer(0) = 1
    var runningSum: Float = 0
    tau = 1
    while (tau < yinBuffer.length) {
      runningSum += yinBuffer(tau)
      yinBuffer(tau) *= tau / runningSum

      tau += 1
    }
  }

  /**
    * Implements step 4 of the AUBIO_YIN paper.
    */
  private [this] def absoluteThreshold(): Int = { // Uses another loop construct
    // than the AUBIO implementation
    var tau = 0
    // first two positions in yinBuffer are always 1
    // So start at the third (index 2)
    tau = 2
    var found = false
    while (!found && tau < yinBuffer.length) {
      if (yinBuffer(tau) < yinThreshold) {
        while ( tau + 1 < yinBuffer.length && yinBuffer(tau + 1) < yinBuffer(tau))
          tau += 1
        // found tau, exit loop and return
        // store the probability
        // From the YIN paper: The threshold determines the list of
        // candidates admitted to the set, and can be interpreted as the
        // proportion of aperiodic power tolerated
        // within a periodic signal.
        //
        // Since we want the periodicity and and not aperiodicity:
        // periodicity = 1 - aperiodicity
        //result.setProbability(1 - yinBuffer(tau))
        probability = 1 - yinBuffer(tau)
        found = true //break
      }

      tau += 1
    }

    // if no pitch found, tau => -1
    if (tau == yinBuffer.length || yinBuffer(tau) >= yinThreshold || probability > 1.0) {
      tau = -1
      probability = 0
      pitched = false
    }
    else
      pitched = true
    tau
  }

  /**
    * Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau
    * value using parabolic interpolation. This is needed to detect higher
    * frequencies more precisely. See http://fizyka.umk.pl/nrbook/c10-2.pdf and
    * for more background
    * http://fedc.wiwi.hu-berlin.de/xplore/tutorials/xegbohtmlnode62.html
    *
    * @param tauEstimate
    * The estimated tau value.
    * @return A better, more precise tau value.
    */
  private def parabolicInterpolation(tauEstimate: Int): Float = {
    var betterTau = .0f
    var x0 = 0
    var x2 = 0
    if (tauEstimate < 1)
      x0 = tauEstimate
    else
      x0 = tauEstimate - 1
    if (tauEstimate + 1 < yinBuffer.length)
      x2 = tauEstimate + 1
    else
      x2 = tauEstimate
    if (x0 == tauEstimate)
      if (yinBuffer(tauEstimate) <= yinBuffer(x2))
        betterTau = tauEstimate
      else
        betterTau = x2
    else if (x2 == tauEstimate)
      if (yinBuffer(tauEstimate) <= yinBuffer(x0))
        betterTau = tauEstimate
      else
        betterTau = x0
    else {
      val s0 = yinBuffer(x0)
      val s1 = yinBuffer(tauEstimate)
      val s2 = yinBuffer(x2)
      // fixed AUBIO implementation, thanks to Karl Helgason:
      // (2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
      betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0))
    }
    betterTau
  }

  /**
    * Calculate the outputs for a given window. Obviously everything is mutating while this is called
    */
  override def calcPitch(): Unit = {
    var tauEstimate = 0
    var pitchInHertz = 0.0f

    // step 2
    difference()

    // step 3
    cumulativeMeanNormalizedDifference()

    // step 4
    tauEstimate = absoluteThreshold()

    // step 5
    if (tauEstimate != -1) {
      val betterTau = parabolicInterpolation(tauEstimate)
      // step 6
      // TODO Implement optimization for the AUBIO_YIN algorithm.
      // 0.77% => 0.5% error rate,
      // using the data of the YIN paper
      // bestLocalEstimate()
      // conversion to Hz
      pitchInHertz = sampleRate / betterTau
    }
    else { // no pitch found
      pitchInHertz = -1
    }

    pitch = pitchInHertz
  }
}
