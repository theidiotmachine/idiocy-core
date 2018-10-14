package idiocy.dsp.ugens

import be.tarsos.dsp.io.{TarsosDSPAudioFloatConverter, TarsosDSPAudioFormat}
import idiocy.dsp.core.{Engine, SignalBufferIn}
import javax.sound.sampled.{AudioFormat, AudioSystem, DataLine, SourceDataLine}


/**
  *
  * @param in
  * @param format
  */
class SpeakersMono(e: Engine, val in: SignalBufferIn, format: AudioFormat) extends UGen(e) with UGenSignalOut0 {
  if(format.getChannels != 1)
    throw new IllegalArgumentException("Audio format is not mono")
  private [this] val byteArray: Array[Byte] = new Array[Byte](in.sz * format.getFrameSize)
  private [this] val bi: Int = in.linkTo(format.getSampleRate.toInt)

  val tarsosDSPAudioFormat: TarsosDSPAudioFormat = TarsosDSPAudioFormat.convertToTarsos(format)
  private [this] val conv: TarsosDSPAudioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(
    tarsosDSPAudioFormat
  )

  private [this] val info: DataLine.Info = new DataLine.Info(classOf[SourceDataLine], format)
  private [this] val line: SourceDataLine = AudioSystem.getLine(info).asInstanceOf[SourceDataLine]
  line.open(format, in.sz * format.getFrameSize)
  line.start()



  override def runInternal(): Int = {
    val howMuch = math.min(in.readCapacity(bi), line.available() / format.getFrameSize)

    //convert to bytes
    val endStubSize = in.readEndStubSize(howMuch, bi)
    val beginStubSize = in.readBeginStubSize(endStubSize, howMuch)
    val lr = in.readPtr(bi)
    if(endStubSize > 0)
      conv.toByteArray(in.b, lr, endStubSize, byteArray, 0)
    if(beginStubSize > 0)
      conv.toByteArray(in.b, 0, beginStubSize, byteArray, endStubSize * format.getFrameSize)

    //write
    val bytesWritten = line.write(byteArray, 0, howMuch * format.getFrameSize)
    in.commitRead(bytesWritten / format.getFrameSize, bi)
    howMuch
  }
}
