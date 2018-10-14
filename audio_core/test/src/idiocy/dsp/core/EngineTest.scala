package idiocy.dsp.core

import be.tarsos.dsp.io.{TarsosDSPAudioFloatConverter, TarsosDSPAudioFormat}
import idiocy.dsp.ugens.{Identity, NoiseGenerator, NullOut, SpeakersMono}
import javax.sound.sampled.AudioFormat
import utest._

object EngineTest extends TestSuite {
  val tests = Tests {

    'test1 - {
      val sampleRate = 48000
      val e = new Engine(0.1f, sampleRate)
      val noise = new NoiseGenerator(e, sampleRate)
      val id = new Identity(e, noise.out)
      val to = new NullOut(e, id.out)

      var i = 0
      while (i < 1000) {
        e.runOnce()
        i += 1
      }
    }

    'test2 - {
      val sampleRate = 44100
      val e = new Engine(0.1f, sampleRate)
      val noise = new NoiseGenerator(e, sampleRate)
      val id = new Identity(e, noise.out)
      val to = new SpeakersMono(e, id.out, new AudioFormat(44100, 16, 1, true, false))

      var i = 0
      while (i < 1000) {
        e.runOnce()
        i += 1
      }
    }

    'test3 - {
      val sampleRate: Int = 44100
      val af = new AudioFormat(sampleRate, 16, 1, true, false)
      val conv = TarsosDSPAudioFloatConverter.getConverter(TarsosDSPAudioFormat.convertToTarsos(af))
      val e = new Engine(0.01f, sampleRate)
      val gen: Int => Float = t => {
        t
      }
      val tst = new MonoConstTestGenerator(e, sampleRate.toInt,
        gen
      )
      val tempFloatArray: Array[Float] = new Array(1)
      tempFloatArray(0) = 1.0f
      val tempByteArray: Array[Byte] = new Array(2)
      val to = new MonoTestOut(e, tst.out, af,
        samples => samples % tst.out.sz,
        (buffer, howMuch, samples, howManySamples) => {
          assert(howMuch == howManySamples * 2)
          var i = 0
          while (i < howMuch) {
            tempFloatArray(0) = gen(samples + i / 2)
            conv.toByteArray(tempFloatArray, 1, tempByteArray)
            assert(buffer(i) == tempByteArray(0))
            i = i + 1
            assert(buffer(i) == tempByteArray(1))
            i = i + 1
          }
          howMuch
        }
      )

      var j = 0
      while (j < 10000) {
        e.runOnce()
        j += 1
      }
    }
  }
}
