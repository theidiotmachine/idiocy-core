package idiocy.dsp.core

import idiocy.dsp.volume.PeakDetector
import utest._

object VolumeDetectorTest extends TestSuite {
  val tests = Tests {

    'testPD - {
      val vd = new PeakDetector(8)
      assert(vd.apply(0.5f) == 0.5f)
      assert(vd.apply(0.2f) == 0.5f)
      assert(vd.apply(0.1f) == 0.5f)
      assert(vd.apply(0.4f) == 0.5f)
      assert(vd.apply(0.3f) == 0.5f)
      assert(vd.apply(0.1f) == 0.5f)
      assert(vd.apply(0.1f) == 0.5f)
      assert(vd.apply(0.1f) == 0.5f)
      assert(vd.apply(0.1f) == 0.4f)
      assert(vd.apply(0.1f) == 0.4f)
      assert(vd.apply(0.1f) == 0.4f)
      assert(vd.apply(0.1f) == 0.3f)
      assert(vd.apply(0.1f) == 0.1f)
      assert(vd.apply(0.5f) == 0.5f)
      assert(vd.apply(0.6f) == 0.6f)
      assert(vd.apply(0.4f) == 0.6f)
      assert(vd.apply(0.7f) == 0.7f)
      assert(vd.apply(0.5f) == 0.7f)
      assert(vd.apply(0.4f) == 0.7f)
      assert(vd.apply(0.6f) == 0.7f)
      assert(vd.apply(0.1f) == 0.7f)
      assert(vd.apply(0.1f) == 0.7f)
      assert(vd.apply(0.1f) == 0.7f)
      assert(vd.apply(0.1f) == 0.7f)
      assert(vd.apply(0.1f) == 0.6f)
    }
  }
}
