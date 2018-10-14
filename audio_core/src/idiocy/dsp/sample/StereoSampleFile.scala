package idiocy.dsp.sample

class StereoSampleFile(fName: String) extends StereoSampleFileBase {

  override var sampleRate: Int = _
  override var lBuffer: Array[Float] = _
  override var rBuffer: Array[Float] = _

  load(fName)

}
