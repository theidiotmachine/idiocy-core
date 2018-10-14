package idiocy.dsp.ugens

import be.tarsos.dsp.io.{TarsosDSPAudioFloatConverter, TarsosDSPAudioFormat}
import idiocy.dsp.core.{Engine, SignalBufferIn}
import javax.sound.sampled.{AudioFormat, AudioSystem, DataLine, SourceDataLine}

class SpeakersStereo(e: Engine, val leftIn: SignalBufferIn, val rightIn: SignalBufferIn, format: AudioFormat)
  extends UGen(e) with UGenSignalOut0 {
  if(format.getChannels != 2)
    throw new IllegalArgumentException("Audio format is not stereo")
  private [this] val byteArray: Array[Byte] = new Array[Byte](math.max(leftIn.sz, rightIn.sz) * format.getFrameSize * 2)
  private [this] val floatArray: Array[Float] = new Array[Float](math.max(leftIn.sz, rightIn.sz) * 2)
  val bil: Int = leftIn.linkTo(format.getSampleRate.toInt)
  val bir: Int = rightIn.linkTo(format.getSampleRate.toInt)


  val tarsosDSPAudioFormat: TarsosDSPAudioFormat = TarsosDSPAudioFormat.convertToTarsos(format)
  private [this] val conv: TarsosDSPAudioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(
    tarsosDSPAudioFormat
  )

  private [this] val info: DataLine.Info = new DataLine.Info(classOf[SourceDataLine], format)
  private [this] val line: SourceDataLine = AudioSystem.getLine(info).asInstanceOf[SourceDataLine]
  line.open(format, math.max(leftIn.sz, rightIn.sz) * format.getFrameSize * 4)
  line.start()

  override def runInternal(): Int = {
    val howMuch = math.min(math.min(leftIn.readCapacity(bil), rightIn.readCapacity(bir)), line.available() / format.getFrameSize)

    //this could probably be done with very clever vector maths but
    var i = 0
    var idx = 0
    var ill = leftIn.readPtr(bil)
    var irl = rightIn.readPtr(bir)
    while(i < howMuch) {
      floatArray(idx) = leftIn.b(ill)
      floatArray(idx+1) = rightIn.b(irl)

      ill = leftIn.nextIndex(ill)
      irl = rightIn.nextIndex(irl)
      idx += 2
      i += 1
    }
    conv.toByteArray(floatArray, howMuch*2, byteArray)
    val bytesWritten = line.write(byteArray, 0, howMuch * format.getFrameSize)
    leftIn.commitRead(bytesWritten / format.getFrameSize, bil)
    rightIn.commitRead(bytesWritten / format.getFrameSize, bir)

    howMuch
  }
}
