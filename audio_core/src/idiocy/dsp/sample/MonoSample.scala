package idiocy.dsp.sample

import idiocy.dsp.interpolation.InterpolationAlgorithm
import javax.sound.sampled.AudioFormat

class MonoSample(val format: AudioFormat, val buffer: Array[Float]) {
  def interpolate(d: Double, interpolationAlgorithm: InterpolationAlgorithm): Float = {
    interpolationAlgorithm.interpolate(buffer, d)
  }
}
