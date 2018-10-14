package idiocy.dsp.core

import idiocy.dsp.dynamicRange.{Gate, HardKneeDownwardCompressor, Limiter}
import utest._

object DynamicRangeTest extends TestSuite{
  val tests = Tests {
    'testGate - {
      val dr = new Gate(0.4f)

      assert(dr.triggered(0.1f))
      assert(dr.apply(0.9f, 0.1f) == 0)

      assert(!dr.triggered(0.9f))
      assert(dr.apply(0.1f, 0.9f) == 0.1f)
    }

    'testHKDC - {
      val dr = new HardKneeDownwardCompressor(0.4f, 0.8f)

      assert(!dr.triggered(0.3f))
      assert(dr.apply(0.1f, 0.3f) == 0.1f)

      assert(dr.triggered(0.5f))
      assert(math.abs(dr.apply(1.0f, 1.0f) - 0.8f) < 0.001f)
      assert(dr.apply(0.5f, 1.0f) == 0.5f * 0.8f)
      assert(math.abs(dr.apply(0.3f, 0.5f) - 0.3f * (0.4f + 0.1f * (0.4f / 0.6f)) / 0.5f) < 0.001f)
      assert(math.abs(dr.apply(0.4f, 0.5f) - 0.4f * (0.4f + 0.1f * (0.4f / 0.6f)) / 0.5f) < 0.001f)
      assert(dr.apply(0.0f, 0.5f) == 0.0f)
    }

    'testLimiter - {
      val dr = new Limiter(0.4f)

      assert(!dr.triggered(0.3f))
      assert(dr.apply(0.1f, 0.3f) == 0.1f)

      assert(dr.triggered(0.5f))
      assert(math.abs(dr.apply(1.0f, 1.0f) - 0.4f) < 0.001f)
      assert(dr.apply(0.5f, 1.0f) == 0.5f * 0.4f)
      assert(math.abs(dr.apply(0.3f, 0.5f) - 0.3f * 0.4f / 0.5f) < 0.001f)
      assert(math.abs(dr.apply(0.4f, 0.5f) - 0.4f * 0.4f / 0.5f) < 0.001f)
      assert(dr.apply(0.0f, 0.5f) == 0.0f)
    }
  }
}
