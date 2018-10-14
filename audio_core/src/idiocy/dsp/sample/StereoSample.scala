package idiocy.dsp.sample

import idiocy.dsp.interpolation.InterpolationAlgorithm
import javax.sound.sampled.AudioFormat

trait StereoSample {
  var lBuffer: Array[Float]
  var rBuffer: Array[Float]
  var sampleRate: Int

  def interpolate(buff: Int, d: Double, interpolationAlgorithm: InterpolationAlgorithm): Float = {
    val b = if(buff == 0) lBuffer else rBuffer
    interpolationAlgorithm.interpolate(b, d)
  }
}
