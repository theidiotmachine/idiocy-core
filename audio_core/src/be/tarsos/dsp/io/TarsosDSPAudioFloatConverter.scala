
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
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package be.tarsos.dsp.io

abstract class TarsosDSPAudioFloatConverter(val format: TarsosDSPAudioFormat) {
  def toFloatArray(in_buff: Array[Byte], in_offset: Int, out_buff: Array[Float], out_offset: Int, out_len: Int): Array[Float]

  def toFloatArray(in_buff: Array[Byte], out_buff: Array[Float], out_offset: Int, out_len: Int): Array[Float] = toFloatArray(in_buff, 0, out_buff, out_offset, out_len)

  def toFloatArray(in_buff: Array[Byte], in_offset: Int, out_buff: Array[Float], out_len: Int): Array[Float] = toFloatArray(in_buff, in_offset, out_buff, 0, out_len)

  def toFloatArray(in_buff: Array[Byte], out_buff: Array[Float], out_len: Int): Array[Float] = toFloatArray(in_buff, 0, out_buff, 0, out_len)

  def toFloatArray(in_buff: Array[Byte], out_buff: Array[Float]): Array[Float] = toFloatArray(in_buff, 0, out_buff, 0, out_buff.length)

  def toByteArray(in_buff: Array[Float], in_offset: Int, in_len: Int, out_buff: Array[Byte], out_offset: Int): Array[Byte]

  def toByteArray(in_buff: Array[Float], in_len: Int, out_buff: Array[Byte], out_offset: Int): Array[Byte] = toByteArray(in_buff, 0, in_len, out_buff, out_offset)

  def toByteArray(in_buff: Array[Float], in_offset: Int, in_len: Int, out_buff: Array[Byte]): Array[Byte] = toByteArray(in_buff, in_offset, in_len, out_buff, 0)

  def toByteArray(in_buff: Array[Float], in_len: Int, out_buff: Array[Byte]): Array[Byte] = toByteArray(in_buff, 0, in_len, out_buff, 0)

  def toByteArray(in_buff: Array[Float], out_buff: Array[Byte]): Array[Byte] = toByteArray(in_buff, 0, in_buff.length, out_buff, 0)
}



object TarsosDSPAudioFloatConverter{

  /** *************************************************************************
    *
    * LSB Filter, used filter least significant byte in samples arrays.
    *
    * Is used filter out data in lsb byte when SampleSizeInBits is not
    * dividable by 8.
    *
    * *************************************************************************/

  class AudioFloatLSBFilter(var converter: TarsosDSPAudioFloatConverter, format: TarsosDSPAudioFormat) extends TarsosDSPAudioFloatConverter(format) {
    final private var mask_buffer: Array[Byte] = _

    final private val stepsize: Int = (format.sampleSizeInBits + 7) / 8
    final private val offset: Int = if (format.bigEndian) stepsize - 1 else 0
    final private val mask: Byte = {
      val lsb_bits = format.sampleSizeInBits % 8
      if (lsb_bits == 0) 0x00.toByte
      else if (lsb_bits == 1) 0x80.toByte
      else if (lsb_bits == 2) 0xC0.toByte
      else if (lsb_bits == 3) 0xE0.toByte
      else if (lsb_bits == 4) 0xF0.toByte
      else if (lsb_bits == 5) 0xF8.toByte
      else if (lsb_bits == 6) 0xFC.toByte
      else if (lsb_bits == 7) 0xFE.toByte
      else 0xFF.toByte
    }

    override def toByteArray(in_buff: Array[Float], in_offset: Int, in_len: Int, out_buff: Array[Byte], out_offset: Int): Array[Byte] = {
      val ret = converter.toByteArray(in_buff, in_offset, in_len, out_buff, out_offset)
      val out_offset_end = in_len * stepsize
      var i = out_offset + offset
      while ( i < out_offset_end) {
        out_buff(i) = (out_buff(i) & mask).toByte

        i += stepsize
      }
      ret
    }

    override def toFloatArray(in_buff: Array[Byte], in_offset: Int, out_buff: Array[Float], out_offset: Int, out_len: Int): Array[Float] = {
      if (mask_buffer == null || mask_buffer.length < in_buff.length) mask_buffer = new Array[Byte](in_buff.length)
      System.arraycopy(in_buff, 0, mask_buffer, 0, in_buff.length)
      val in_offset_end = out_len * stepsize
      var i = in_offset + offset
      while ( {
        i < in_offset_end
      }) {
        mask_buffer(i) = (mask_buffer(i) & mask).toByte

        i += stepsize
      }
      val ret = converter.toFloatArray(mask_buffer, in_offset, out_buff, out_offset, out_len)
      ret
    }
  }

  /** *************************************************************************
    *
    * 16 bit signed/unsigned, little/big-endian
    *
    * *************************************************************************/
  // PCM 16 bit, signed, little-endian
  class AudioFloatConversion16SL(format: TarsosDSPAudioFormat) extends TarsosDSPAudioFloatConverter(format) {
    override def toFloatArray(in_buff: Array[Byte], in_offset: Int, out_buff: Array[Float], out_offset: Int, out_len: Int): Array[Float] = {
      var ix = in_offset
      val len = out_offset + out_len
      var ox = out_offset
      //ugh no increment
      while ( ox < len) {

        out_buff(ox) = ((in_buff(ix) & 0xFF) | (in_buff(ix + 1) << 8)).toShort * (1.0f / 32767.0f)
        ix += 2
        ox += 1
        ox - 1
      }
      out_buff
    }

    override def toByteArray(in_buff: Array[Float], in_offset: Int, in_len: Int, out_buff: Array[Byte], out_offset: Int): Array[Byte] = {
      var ox = out_offset
      val len = in_offset + in_len
      var ix = in_offset
      while (ix < len) {
        val x = (in_buff(ix) * 32767.0).toInt
        out_buff(//{
          //ox += 1; ox - 1
        //}
          ox
        ) = x.toByte
        out_buff(//{
          //ox += 1; ox - 1
          ox + 1
        //}
        ) = (x >>> 8).toByte
        ox += 2
        ix += 1
        ix - 1
      }
      out_buff
    }
  }

  /** *************************************************************************
    *
    * 24 bit signed/unsigned, little/big-endian
    *
    * *************************************************************************/
  // PCM 24 bit, signed, little-endian
  class AudioFloatConversion24SL(format: TarsosDSPAudioFormat) extends TarsosDSPAudioFloatConverter(format) {
    override def toFloatArray(in_buff: Array[Byte], in_offset: Int, out_buff: Array[Float], out_offset: Int, out_len: Int): Array[Float] = {
      var ix = in_offset
      var ox = out_offset
      var i = 0
      while (
        i < out_len
      ) {
        var x = (in_buff({
          ix += 1; ix - 1
        }) & 0xFF) | ((in_buff({
          ix += 1; ix - 1
        }) & 0xFF) << 8) | ((in_buff({
          ix += 1; ix - 1
        }) & 0xFF) << 16)
        if (x > 0x7FFFFF) x -= 0x1000000
        out_buff({
          ox += 1; ox - 1
        }) = x * (1.0f / 0x7FFFFF.toFloat)


        i += 1
        i - 1

      }
      out_buff
    }

    override def toByteArray(in_buff: Array[Float], in_offset: Int, in_len: Int, out_buff: Array[Byte], out_offset: Int): Array[Byte] = {
      var ix = in_offset
      var ox = out_offset
      var i = 0
      while (
        i < in_len
      ) {
        var x = (in_buff({
          ix += 1; ix - 1
        }) * 0x7FFFFF.toFloat).toInt
        if (x < 0) x += 0x1000000
        out_buff({
          ox += 1; ox - 1
        }) = x.toByte
        out_buff({
          ox += 1; ox - 1
        }) = (x >>> 8).toByte
        out_buff({
          ox += 1; ox - 1
        }) = (x >>> 16).toByte

        i += 1
        i - 1
      }
      out_buff
    }
  }


  def getConverter(tarsosFormat: TarsosDSPAudioFormat): TarsosDSPAudioFloatConverter = {
    var conv: TarsosDSPAudioFloatConverter = null
    if (tarsosFormat.encoding.equals(TarsosDSPAudioFormatEncoding.PCM_SIGNED)) {
      if (tarsosFormat.bigEndian) {
        /*
        if (format.sampleSizeInBits <= 8)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion8S
        else if (format.sampleSizeInBits > 8 && format.sampleSizeInBits <= 16)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion16SB
        else if (format.sampleSizeInBits > 16 && format.sampleSizeInBits <= 24)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion24SB
        else if (format.sampleSizeInBits > 24 && format.sampleSizeInBits <= 32)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32SB
        else if (format.sampleSizeInBits > 32)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32xSB(((format.sampleSizeInBits + 7) / 8) - 4)
          */
        ???
      }
      else {
        if (tarsosFormat.sampleSizeInBits <= 8)
          //conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion8S
          ???
        else if (tarsosFormat.sampleSizeInBits > 8 && tarsosFormat.sampleSizeInBits <= 16)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion16SL(tarsosFormat)
        else if (tarsosFormat.sampleSizeInBits > 16 && tarsosFormat.sampleSizeInBits <= 24)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion24SL(tarsosFormat)
        else if (tarsosFormat.sampleSizeInBits > 24 && tarsosFormat.sampleSizeInBits <= 32)
          //conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32SL
          ???
        else if (tarsosFormat.sampleSizeInBits > 32)
          //conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32xSL(((format.sampleSizeInBits + 7) / 8) - 4)
          ???
      }
    } else if (tarsosFormat.encoding.equals(TarsosDSPAudioFormatEncoding.PCM_UNSIGNED)) {
      /*
      if (format.bigEndian) {
        if (format.sampleSizeInBits <= 8)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion8U
        else if (format.sampleSizeInBits > 8 && format.sampleSizeInBits <= 16)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion16UB
        else if (format.sampleSizeInBits > 16 && format.sampleSizeInBits <= 24)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion24UB
        else if (format.sampleSizeInBits > 24 && format.sampleSizeInBits <= 32)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32UB
        else if (format.sampleSizeInBits > 32)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32xUB(((format.sampleSizeInBits + 7) / 8) - 4)
      } else {
        if (format.sampleSizeInBits <= 8)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion8U
        else if (format.sampleSizeInBits > 8 && format.sampleSizeInBits <= 16)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion16UL
        else if (format.sampleSizeInBits > 16 && format.sampleSizeInBits <= 24)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion24UL
        else if (format.sampleSizeInBits > 24 && format.sampleSizeInBits <= 32)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32UL
        else if (format.sampleSizeInBits > 32)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32xUL(((format.sampleSizeInBits + 7) / 8) - 4)
      }
      */
      ???
    }
    else if (tarsosFormat.encoding.equals(TarsosDSPAudioFormatEncoding.PCM_FLOAT)) {
      /*
      if (format.sampleSizeInBits == 32) {
        if (format.bigEndian)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32B
        else
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion32L
      }
      else if (format.sampleSizeInBits == 64) {
        if (format.bigEndian)
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion64B
        else
          conv = new TarsosDSPAudioFloatConverter.AudioFloatConversion64L
      }
      */
      ???
    }

    if (
      (tarsosFormat.encoding.equals(TarsosDSPAudioFormatEncoding.PCM_SIGNED)
        || tarsosFormat.encoding.equals(TarsosDSPAudioFormatEncoding.PCM_UNSIGNED))
        && (tarsosFormat.sampleSizeInBits % 8 != 0))
      conv = new TarsosDSPAudioFloatConverter.AudioFloatLSBFilter(conv, tarsosFormat)

    /*
    if (conv != null)
      conv.format = format
     */
    conv
  }
}
