package idiocy.dsp.sample

abstract class StereoSampleFileBase extends StereoSample {
 protected [this] def load(fName: String): Unit = {
   val (lSampleRate, lLBuffer, lRBuffer) = SampleLoader.loadStereo(fName)
   sampleRate = lSampleRate
   lBuffer = lLBuffer
   rBuffer = lRBuffer
 }
}
