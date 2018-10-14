package idiocy.dsp.core

import be.tarsos.dsp.io.{TarsosDSPAudioFloatConverter, TarsosDSPAudioFormat}
import idiocy.dsp.ugens.{UGen, UGenSignalOut0}
import javax.sound.sampled.AudioFormat

class MonoTestOut(e: Engine, val in: SignalBufferIn, format: AudioFormat,
                  fakeAvailable: (Int)=>Int,
                  fakeWrite: (Array[Byte], Int, Int, Int)=>Int)
  extends UGen(e) with UGenSignalOut0{

  val byteArray: Array[Byte] = new Array[Byte](in.sz * format.getFrameSize)
  val bi: Int = in.linkTo(format.getSampleRate.toInt)

  val tarsosDSPAudioFormat: TarsosDSPAudioFormat = TarsosDSPAudioFormat.convertToTarsos(format)
  val conv: TarsosDSPAudioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(
    tarsosDSPAudioFormat
  )

  var samples: Int = 0

  override def runInternal(): Int = {
    val lineAvailable = fakeAvailable(samples)
    val howMuch = math.min(in.readCapacity(bi), lineAvailable / format.getFrameSize)

    //convert to bytes
    val endStubSize = in.readEndStubSize(howMuch, bi)
    val beginStubSize = in.readBeginStubSize(endStubSize, howMuch)
    val lr = in.readPtr(bi)
    conv.toByteArray(in.b, lr, endStubSize, byteArray, 0)
    conv.toByteArray(in.b, 0, beginStubSize, byteArray, endStubSize * format.getFrameSize)

    //write
    val bytesWritten = fakeWrite(byteArray, howMuch * format.getFrameSize, samples, howMuch)
    samples += howMuch
    in.commitRead(bytesWritten / format.getFrameSize, bi)

    howMuch
  }
}
