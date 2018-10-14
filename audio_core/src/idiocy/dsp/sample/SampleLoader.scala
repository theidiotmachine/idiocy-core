package idiocy.dsp.sample

import java.io.File

import be.tarsos.dsp.io.{TarsosDSPAudioFloatConverter, TarsosDSPAudioFormat}
import io.nayuki.flac.common.StreamInfo
import io.nayuki.flac.decode.FlacDecoder
import javax.sound.sampled.{AudioFileFormat, AudioFormat, AudioInputStream, AudioSystem}

object SampleLoader {
  def loadWav(fileName: String): (AudioFormat, Array[Float], Int) = {
    val classLoader = getClass.getClassLoader
    val loadedFile = new File(classLoader.getResource(fileName).getFile)


    val fileFormat: AudioFileFormat = AudioSystem.getAudioFileFormat(loadedFile)
    val format = fileFormat.getFormat

    val stream: AudioInputStream = AudioSystem.getAudioInputStream(loadedFile)
    val thisBuffer: Array[Float] = new Array(stream.getFrameLength.toInt * format.getChannels)

    val l: Int = stream.getFrameLength.toInt * format.getFrameSize
    val b: Array[Byte] = new Array[Byte](l)
    stream.read(b, 0, l)

    val tarsosDSPAudioFormat: TarsosDSPAudioFormat = TarsosDSPAudioFormat.convertToTarsos(format)
    val conv: TarsosDSPAudioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(
      tarsosDSPAudioFormat
    )

    conv.toFloatArray(b, thisBuffer)
    (format, thisBuffer, stream.getFrameLength.toInt)
  }

  private [this] def loadFlac(fileName: String): (Int, Int, Array[Array[Int]]) = {

    val classLoader = getClass.getClassLoader
    val inFile = new File(classLoader.getResource(fileName).getFile)

    var streamInfo: StreamInfo = null
    val dec = new FlacDecoder(inFile)
    while (dec.readAndHandleMetadataBlock != null) {}
    streamInfo = dec.streamInfo
    if (streamInfo.sampleDepth % 8 != 0)
      throw new UnsupportedOperationException("Only whole-byte sample depth supported")

    // Decode every block
    val samples: Array[Array[Int]] = Array.ofDim(streamInfo.numChannels, streamInfo.numSamples.toInt)
    //val samples = new Array[Array[Int]](streamInfo.numChannels, streamInfo.numSamples.toInt)
    var off = 0
    var reading = true
    while (reading) {
      val len = dec.readAudioBlock(samples, off)
      if (len == 0) {
        reading = false
      } else
        off += len
    }
    //flac - all numbers are big endian, unsigned
    (streamInfo.sampleRate, streamInfo.sampleDepth, samples)
  }

  private [this] def loadStereoWav(fileName: String): (Int, Array[Float], Array[Float]) = {
    val (format, thisBuffer, frameLength) = loadWav(fileName)
    if(format.getChannels == 1) {
      (format.getSampleRate.toInt, thisBuffer, thisBuffer)
    } else {
      val howMuch = frameLength
      val lout: Array[Float] = new Array(howMuch)
      var rout: Array[Float] = new Array(howMuch)
      var o = 0
      var i = 0
      while(o < howMuch){
        lout(o)  = thisBuffer(i)
        i += 1
        rout(o) = thisBuffer(i)
        i += 1
        o += 1
      }
      (format.getSampleRate.toInt, lout, rout)
    }
  }

  private [this] def loadStereoFlac(fileName: String): (Int, Array[Float], Array[Float]) = {
    val (sampleRate, sampleDepth, samples) = loadFlac(fileName)
    if(samples.length == 2) {
      val lBuffer = new Array[Float](samples(0).length)
      val rBuffer = new Array[Float](samples(0).length)
      val buffers = Array(lBuffer, rBuffer)
      val mul = 1 << (sampleDepth - 1)
      var i = 0
      while(i < samples.length){
        var j = 0
        while(j < samples(i).length){
          buffers(i)(j) = samples(i)(j).toFloat / mul
          j += 1
        }
        i += 1
      }
      (sampleRate, lBuffer, rBuffer)
    } else if (samples.length == 1) {
      val buffer = new Array[Float](samples(0).length)
      val mul = 1 << (sampleDepth - 1)
      var j = 0
      while(j < samples(0).length){
        buffer(j) = samples(0)(j).toFloat / mul
        j += 1
      }
      (sampleRate, buffer, buffer)
    } else
      throw new IllegalArgumentException("Can't process file with > 2 channels")
  }

  def loadStereo(fileName: String): (Int, Array[Float], Array[Float]) = {
    if(fileName.toLowerCase.endsWith(".wav"))
      loadStereoWav(fileName)
    else if(fileName.toLowerCase.endsWith(".flac")) {
      loadStereoFlac(fileName)
    } else
      throw new IllegalArgumentException("file type not supported")
  }
}
