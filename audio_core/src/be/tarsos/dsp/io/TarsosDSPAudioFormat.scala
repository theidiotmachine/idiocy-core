package be.tarsos.dsp.io

import javax.sound.sampled.AudioFormat

/*
This was a port from the original Tarsos dsp code. It contained the following licence
 */

/*
*      _______                       _____   _____ _____
*     |__   __|                     |  __ \ / ____|  __ \
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
*
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
*
*/

/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


/**
<code>AudioFormat</code> is the class that specifies a particular arrangement of data in a sound stream.
  * By examing the information stored in the audio format, you can discover how to interpret the bits in the
  * binary sound data.
  * <p>
  * Every data LineWavelet has an audio format associated with its data stream. The audio format of a source (playback) data LineWavelet indicates
  * what kind of data the data LineWavelet expects to receive for output.  For a target (capture) data LineWavelet, the audio format specifies the kind
  * of the data that can be read from the LineWavelet.
  * Sound files also have audio formats, of course.
  * <p>
  * The <code>AudioFormat</code> class accommodates a number of common sound-file encoding techniques, including
  * pulse-code modulation (PCM), mu-law encoding, and a-law encoding.  These encoding techniques are predefined,
  * but service providers can create new encoding types.
  * The encoding that a specific format uses is named by its <code>encoding</code> field.
  * <p>
  * In addition to the encoding, the audio format includes other properties that further specify the exact
  * arrangement of the data.
  * These include the number of channels, sample rate, sample size, byte order, frame rate, and frame size.
  * Sounds may have different numbers of audio channels: one for mono, two for stereo.
  * The sample rate measures how many "snapshots" (samples) of the sound pressure are taken per second, per channel.
  * (If the sound is stereo rather than mono, two samples are actually measured at each instant of time: one for the left channel,
  * and another for the right channel; however, the sample rate still measures the number per channel, so the rate is the same
  * regardless of the number of channels.   This is the standard use of the term.)
  * The sample size indicates how many bits are used to store each snapshot; 8 and 16 are typical values.
  * For 16-bit samples (or any other sample size larger than a byte),
  * byte order is important; the bytes in each sample are arranged in
  * either the "little-endian" or "big-endian" style.
  * For encodings like PCM, a frame consists of the set of samples for all channels at a given
  * point in time, and so the size of a frame (in bytes) is always equal to the size of a sample (in bytes) times
  * the number of channels.  However, with some other sorts of encodings a frame can contain
  * a bundle of compressed data for a whole series of samples, as well as additional, non-sample
  * data.  For such encodings, the sample rate and sample size refer to the data after it is decoded into PCM,
  * and so they are completely different from the frame rate and frame size.
  *
  * <p>An <code>AudioFormat</code> object can include a set of
  * properties. A property is a pair of key and value: the key
  * is of type <code>String</code>, the associated property
  * value is an arbitrary object. Properties specify
  * additional format specifications, like the bit rate for
  * compressed formats. Properties are mainly used as a means
  * to transport additional information of the audio format
  * to and from the service providers. Therefore, properties
  * are ignored in the AudioFormat method.
  *
  * <p>The following table lists some common properties which
  * service providers should use, if applicable:
  *
  * <table border=0>
  * <tr>
  * <th>Property key</th>
  * <th>Value type</th>
  * <th>Description</th>
  * </tr>
  * <tr>
  * <td>&quot;bitrate&quot;</td>
  * <td></td>
  * <td>average bit rate in bits per second</td>
  * </tr>
  * <tr>
  * <td>&quot;vbr&quot;</td>
  * <td></td>
  * <td><code>true</code>, if the file is encoded in variable bit
  * rate (VBR)</td>
  * </tr>
  * <tr>
  * <td>&quot;quality&quot;</td>
  * <td></td>
  * <td>encoding/conversion quality, 1..100</td>
  * </tr>
  * </table>
  *
  * <p>Vendors of service providers (plugins) are encouraged
  * to seek information about other already established
  * properties in third party plugins, and follow the same
  * conventions.
  *
  * @author Kara Kytle
  * @author Florian Bomers
  * @since 1.3
  *
  *
  * @param encoding           The audio encoding technique used by this format.
  * @param sampleRate         The number of samples played or recorded per second, for sounds that have this format.
  * @param sampleSizeInBits   The number of bits in each sample of a sound that has this format.
  * @param channels           The number of audio channels in this format (1 for mono, 2 for stereo).
  * @param bigEndian          Indicates whether the audio data is stored in big-endian or little-endian order.
  * @param frameRate          The number of frames played or recorded per second, for sounds that have this format.
  * @param properties         The set of properties
  */
class TarsosDSPAudioFormat(
                            val encoding: TarsosDSPAudioFormatEncoding,
                            val sampleRate: Float,
                            val sampleSizeInBits: Int,
                            val channels: Int,
                            val frameRate: Float,
                            val bigEndian: Boolean,
                            val properties: Map[String, Any] = Map()
                          ) {

  def frameSize: Int = ((sampleSizeInBits + 7) / 8) * channels

  def signed: Boolean = encoding == TarsosDSPAudioFormatEncoding.PCM_SIGNED

  /**
    * Constructs an <code>AudioFormat</code> with a linear PCM encoding and
    * the given parameters.  The frame size is set to the number of bytes
    * required to contain one sample from each channel, and the frame rate
    * is set to the sample rate.
    *
    * @param sampleRate       the number of samples per second
    * @param sampleSizeInBits the number of bits in each sample
    * @param channels         the number of channels (1 for mono, 2 for stereo, and so on)
    * @param signed           indicates whether the data is signed or unsigned
    * @param bigEndian        indicates whether the data for a single sample
    *                         is stored in big-endian byte order (<code>false</code>
    *                         means little-endian)
    */
  def this(sampleRate: Float, sampleSizeInBits: Int, channels: Int, signed: Boolean, bigEndian: Boolean) {
    this( if (signed) {
      TarsosDSPAudioFormatEncoding.PCM_SIGNED
    }
    else {
      TarsosDSPAudioFormatEncoding.PCM_UNSIGNED
    }, sampleRate, sampleSizeInBits, channels,
      sampleRate, bigEndian)
  }
}

object TarsosDSPAudioFormat{
  val FRAME_SIZE_NOT_SPECIFIED: Int = -1
  val FRAME_RATE_NOT_SPECIFIED: Float = -1

  def getAudioFormat(in: TarsosDSPAudioFormat): AudioFormat = {
    new AudioFormat(in.sampleRate, in.sampleSizeInBits, in.channels, in.signed, in.bigEndian)
  }

  def convertToTarsos(in: AudioFormat): TarsosDSPAudioFormat = {
    new TarsosDSPAudioFormat(in.getSampleRate, in.getSampleSizeInBits, in.getChannels,
      in.getEncoding == AudioFormat.Encoding.PCM_SIGNED, in.isBigEndian)
  }
}