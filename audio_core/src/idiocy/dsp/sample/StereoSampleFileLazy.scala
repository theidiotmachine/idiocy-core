package idiocy.dsp.sample

import idiocy.dsp.interpolation.InterpolationAlgorithm

class StereoSampleFileLazy(val fName: String, var sampleRate: Int) extends StereoSampleFileBase {
  override var lBuffer: Array[Float] = _
  override var rBuffer: Array[Float] = _
  private [this] var init = false

  override def interpolate(buff: Int, d: Double, interpolationAlgorithm: InterpolationAlgorithm): Float = {
    if(!init) {
      load(fName)
      init = true
    }
    super.interpolate(buff, d, interpolationAlgorithm)
  }
}
