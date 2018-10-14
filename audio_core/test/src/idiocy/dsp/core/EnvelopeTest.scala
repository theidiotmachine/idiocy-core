package idiocy.dsp.core

import idiocy.dsp.envelope.{EnvelopeHardADSR, EnvelopeHardASR, EnvelopeSR, EnvelopeSoftASR}
import utest._

object EnvelopeTest extends TestSuite {
  val tests = Tests {

    'testEnvelopeHardADSR - {
      val e1 = new EnvelopeHardADSR

      //dormant
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.1f) == 0.0f)

      //trigger om, look for a, d, s
      e1.triggerOn(0.5f, 0.5f, 0.8f)
      //a
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.5f)
      //d
      //assert(e1.apply(0.5f) == 1.0f)
      //assert(e1.apply(0.75f) == 0.9f)
      assert(e1.apply(0.25f) == 0.5f)
      assert(e1.apply(0.0f) == 1.0f)
      //assert(e1.apply(0.75f) == 0.9f)
      assert(e1.apply(0.25f) == 1.0f)
      assert(e1.apply(0.0f) == 0.9f)
      //s
      //assert(e1.apply(1.0f) == 0.8f)
      //assert(e1.apply(1.5f) == 0.8f)
      assert(e1.apply(0.25f) == 0.9f)
      assert(e1.apply(0.0f) == 0.8f)
      assert(e1.apply(1.5f) == 0.8f)
      assert(e1.apply(0.0f) == 0.8f)

      //trigger off, let it finish
      e1.triggerOff(0.5f)
      /*
    assert(e1.apply(0.0f) == 0.8f)
    assert(e1.apply(0.25f) == 0.4f)
    assert(e1.apply(0.5f) == 0.0f)
    assert(e1.apply(1.0f) == 0.0f)
*/
      assert(e1.apply(0.0f) == 0.8f)
      assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.5f) == 0.0f)

      //now retrigger
      e1.triggerOn(0.5f, 0.5f, 0.8f)
      //assert(e1.apply(0.25f) == 0.5f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.5f)

      //trigger off before we finished attacking. It will release in proportional time
      e1.triggerOff(0.5f)
      /*
    assert(e1.apply(0.0f) == 0.5f)
    assert(e1.apply(0.15625f) == 0.25f)
    assert(e1.apply(0.3125f) == 0.0f)
*/
      assert(e1.apply(0.0f) == 0.5f)
      assert(e1.apply(0.15625f) == 0.5f)
      assert(e1.apply(0.3125f - 0.15625f) == 0.25f)
      assert(e1.apply(0.0f) == 0.0f)

      //now retrigger
      e1.triggerOn(0.5f, 0.5f, 0.8f)
      /*
    assert(e1.apply(0.5f) == 1.0f)
    assert(e1.apply(0.75f) == 0.9f)
*/
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.25f) == 1.0f)
      assert(e1.apply(0.0f) == 0.9f)

      //trigger off before we finished decaying. It will release in proportional time
      e1.triggerOff(0.5f)
      /*
    assert(math.abs(e1.apply(0.0f) - 0.9f) < 0.001f)
    assert(math.abs(e1.apply(0.28125f) - 0.45f) < 0.001f)
    assert(e1.apply(0.5625f) == 0.0f)
*/
      assert(math.abs(e1.apply(0.0f) - 0.9f) < 0.001f)
      assert(math.abs(e1.apply(0.28125f) - 0.9f) < 0.001f)
      assert(math.abs(e1.apply(0.5625f - 0.28125f) - 0.45f) < 0.001f)
      assert(math.abs(e1.apply(0.0f) - 0.0f) == 0.0f)

      //show the hardness. retrigger, let it attack a bit
      e1.triggerOn(0.5f, 0.5f, 0.8f)
      //assert(e1.apply(0.25f) == 0.5f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.5f)

      //now retrigger in attack period, show that it has reset.
      e1.triggerOn(0.5f, 0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.0f) == 0.0f)

      //retrigger in decay period, show that it has reset
      //assert(e1.apply(0.5f) == 1.0f)
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.0f) == 1.0f)
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.0f) == 1.0f)
      //assert(e1.apply(0.15625f) == 0.75f)
      assert(e1.apply(0.0f) == 1.0f)
      assert(e1.apply(0.15625f) == 1.0f)
      assert(e1.apply(0.0f) == 0.75f)
      e1.triggerOn(0.5f, 0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.0f)
      //assert(e1.apply(0.5f) == 1.0f)
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.0f) == 1.0f)
    }

    'testEnvelopeHardASR - {
      val e1 = new EnvelopeHardASR
      //assert(e1.apply(0.1f) == 0.0f)
      assert(e1.apply(0.1f) == 0.0f)
      assert(e1.apply(0.0f) == 0.0f)

      //trigger, run a good long time
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.0f)
      //assert(e1.apply(0.25f) == 0.4f)
      //assert(e1.apply(0.5f) == 0.8f)
      //assert(e1.apply(1.0f) == 0.8f)
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.5f) == 0.8f)
      assert(e1.apply(0.0f) == 0.8f)

      //trigger off, let it finish
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.0f) == 0.8f)
      //assert(e1.apply(0.25f) == 0.4f)
      //assert(e1.apply(0.5f) == 0.0f)
      //assert(e1.apply(1.0f) == 0.0f)
      assert(e1.apply(0.0f) == 0.8f)
      assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.0f) == 0.0f)

      //now retrigger
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.4f)

      //trigger off before we finished attacking. It will decay in proportional time
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.0f) == 0.4f)
      //assert(e1.apply(0.125f) == 0.2f)
      //assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.4f)
      assert(e1.apply(0.125f) == 0.4f)
      assert(e1.apply(0.125f) == 0.2f)
      assert(e1.apply(0.0f) == 0.0f)

      //show the hardness. retrigger, let it attack a bit
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.4f)

      //now retrigger in attack period, show that it has reset.
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.0f) == 0.0f)

      //retrigger in release period, show that it has reset
      //assert(e1.apply(0.5f) == 0.8f)
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.0f) == 0.8f)
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.0f) == 0.4f)
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.0f)
      //assert(e1.apply(0.5f) == 0.8f)
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.0f) == 0.8f)
    }

    'testEnvelopeSoftASR - {
      val e1 = new EnvelopeSoftASR

      //dormant
      //assert(e1.apply(0.1f) == 0.0f)
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.1f) == 0.0f)

      //trigger, run a good long time
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.0f)
      //assert(e1.apply(0.25f) == 0.4f)
      //assert(e1.apply(0.5f) == 0.8f)
      //assert(e1.apply(1.0f) == 0.8f)
      assert(e1.apply(0.0f) == 0.0f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.5f) == 0.8f)

      //trigger off, let it finish
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.0f) == 0.8f)
      //assert(e1.apply(0.25f) == 0.4f)
      //assert(e1.apply(0.5f) == 0.0f)
      //assert(e1.apply(1.0f) == 0.0f)
      assert(e1.apply(0.0f) == 0.8f)
      assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.5f) == 0.0f)

      //now retrigger
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.4f)

      //trigger off before we finished attacking. It will decay in proportional time
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.0f) == 0.4f)
      //assert(e1.apply(0.125f) == 0.2f)
      //assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.4f)
      assert(e1.apply(0.125f) == 0.4f)
      assert(e1.apply(0.125f) == 0.2f)
      assert(e1.apply(0.0f) == 0.0f)

      //show the softness. retrigger, let it attack a bit
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.25f) == 0.0f)
      assert(e1.apply(0.0f) == 0.4f)

      //now retrigger in attack period, show that it has not reset.
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.4f)
      //assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.0f) == 0.4f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.0f) == 0.8f)

      //retrigger in release period, show that it has not reset
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.0f) == 0.4f)
      e1.triggerOn(0.5f, 0.8f)
      //assert(e1.apply(0.0f) == 0.4f)
      //assert(e1.apply(0.25f) == 0.8f)
      assert(e1.apply(0.0f) == 0.4f)
      assert(e1.apply(0.25f) == 0.4f)
      assert(e1.apply(0.0f) == 0.8f)
    }


    'testEnvelopeSR - {
      val e1 = new EnvelopeSR()
      //dormant
      //assert(e1.apply(0.1f) == 0.0f)
      assert(e1.apply(0.1f) == 0.0f)
      assert(e1.apply(0.1f) == 0.0f)

      //trigger on
      e1.triggerOn(0.5f)
      //assert(e1.apply(0.0f) == 0.5f)
      //assert(e1.apply(1.0f) == 0.5f)
      assert(e1.apply(0.0f) == 0.5f)
      assert(e1.apply(1.0f) == 0.5f)
      assert(e1.apply(1.0f) == 0.5f)

      //trigger off show it releasing
      e1.triggerOff(0.5f)
      //assert(e1.apply(0.0f) == 0.5f)
      //assert(e1.apply(0.25f) == 0.25f)
      //assert(e1.apply(0.5f) == 0.0f)
      //assert(e1.apply(1.0f) == 0.0f)
      assert(e1.apply(0.0f) == 0.5f)
      assert(e1.apply(0.25f) == 0.5f)
      assert(e1.apply(0.25f) == 0.25f)
      assert(e1.apply(0.5f) == 0.0f)
      assert(e1.apply(0.0f) == 0.0f)
    }
  }
}
