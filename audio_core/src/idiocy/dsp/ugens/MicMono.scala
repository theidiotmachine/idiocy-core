package idiocy.dsp.ugens

import be.tarsos.dsp.io.{TarsosDSPAudioFloatConverter, TarsosDSPAudioFormat}
import idiocy.dsp.core.Engine
import javax.sound.sampled._

class MicMono(e: Engine, format: AudioFormat) extends UGen(e) with UGenSignalOut1 {
  initOut(e, format.getSampleRate.toInt)

  private [this] val byteArray: Array[Byte] = new Array[Byte](e.sz(format.getSampleRate.toInt) * format.getFrameSize)

  val tarsosDSPAudioFormat: TarsosDSPAudioFormat = TarsosDSPAudioFormat.convertToTarsos(format)
  private [this] val conv: TarsosDSPAudioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(
    tarsosDSPAudioFormat
  )

  //private [this] val info: DataLine.Info = new DataLine.Info(classOf[SourceDataLine], format)
  private [this] val line: TargetDataLine = AudioSystem.getTargetDataLine(format)
  line.open(format, e.sz(format.getSampleRate.toInt) * format.getFrameSize)
  line.start()

  private [this] def calcHowMuch: Int = {
    math.min(bufferOut.writeCapacity, line.available() / format.getFrameSize)
  }

  override def runInternal(): Int = {
    var howMuch = calcHowMuch
    val bytesRead = line.read(byteArray, 0, howMuch * format.getFrameSize)
    howMuch = bytesRead / format.getFrameSize

    val wl = bufferOut.writePtr

    val endStubSize = bufferOut.writeEndStubSize(howMuch)
    val beginStubSize = bufferOut.writeBeginStubSize(endStubSize, howMuch)
    if(endStubSize > 0)
      conv.toFloatArray(byteArray, 0, bufferOut.b, wl, endStubSize)
    if(beginStubSize > 0)
      conv.toFloatArray(byteArray, endStubSize * format.getFrameSize, bufferOut.b, 0, beginStubSize)

    bufferOut.commitWrite(howMuch)
    howMuch
  }
}
