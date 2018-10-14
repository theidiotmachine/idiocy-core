package idiocy.dsp.sample

class StereoSampleBuffer(var sampleRate: Int,
                         var lBuffer: Array[Float],
                         var rBuffer: Array[Float]) extends StereoSample {

}
