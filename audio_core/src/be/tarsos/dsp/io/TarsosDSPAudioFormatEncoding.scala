package be.tarsos.dsp.io

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
  * The <code>Encoding</code> class  names the  specific type of data representation
  * used for an audio stream.   The encoding includes aspects of the
  * sound format other than the number of channels, sample rate, sample size,
  * frame rate, frame size, and byte order.
  * <p>
  * One ubiquitous type of audio encoding is pulse-code modulation (PCM),
  * which is simply a linear (proportional) representation of the sound
  * waveform.  With PCM, the number stored in each sample is proportional
  * to the instantaneous amplitude of the sound pressure at that point in
  * time.  The numbers are frequently signed or unsigned integers.
  * Besides PCM, other encodings include mu-law and a-law, which are nonlinear
  * mappings of the sound amplitude that are often used for recording speech.
  * <p>
  * You can use a predefined encoding by referring to one of the static
  * objects created by this class, such as PCM_SIGNED or
  * PCM_UNSIGNED.  Service providers can create new encodings, such as
  * compressed audio formats or floating-point PCM samples, and make
  * these available through the <code>AudioSystem</code> class.
  * <p>
  * The <code>Encoding</code> class is static, so that all
  * <code>AudioFormat</code> objects that have the same encoding will refer
  * to the same object (rather than different instances of the same class).
  * This allows matches to be made by checking that two format's encodings
  * are equal.
  *
  * @author Kara Kytle
  * @since 1.3
  */
final class TarsosDSPAudioFormatEncoding(val name: String) {

  // METHODS

  /**
    * Finalizes the equals method
    */
  override def equals(obj: Any): Boolean = {
    if (toString == null) return (obj != null) && (obj.toString == null)
    if (obj.isInstanceOf[TarsosDSPAudioFormatEncoding]) return toString == obj.toString
    false
  }

  /**
    * Finalizes the hashCode method
    */
  override def hashCode: Int = {
    if (toString == null) return 0
    toString.hashCode
  }

  /**
    * Provides the <code>String</code> representation of the encoding.  This <code>String</code> is
    * the same name that was passed to the constructor.  For the predefined encodings, the name
    * is similar to the encoding's variable (field) name.  For example, <code>PCM_SIGNED.toString()</code> returns
    * the name "pcm_signed".
    *
    * @return the encoding name
    */
  override def toString: String = name
}

object TarsosDSPAudioFormatEncoding {
  // ENCODING DEFINES
  /**
    * Specifies signed, linear PCM data.
    */
  val PCM_SIGNED = new TarsosDSPAudioFormatEncoding("PCM_SIGNED")

  /**
    * Specifies unsigned, linear PCM data.
    */
  val PCM_UNSIGNED = new TarsosDSPAudioFormatEncoding("PCM_UNSIGNED")

  val PCM_FLOAT = new TarsosDSPAudioFormatEncoding("PCM_FLOAT")

  /**
    * Specifies u-law encoded data.
    */
  val ULAW = new TarsosDSPAudioFormatEncoding("ULAW")

  /**
    * Specifies a-law encoded data.
    */
  val ALAW = new TarsosDSPAudioFormatEncoding("ALAW")
}
