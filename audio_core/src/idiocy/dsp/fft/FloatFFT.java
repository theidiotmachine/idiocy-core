package idiocy.dsp.fft;
/*
 * Free FloatFFT and convolution (Java)
 *
 * Copyright (c) 2017 Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/free-small-fft-in-multiple-languages
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */


public final class FloatFFT {

    private int _sz;
    private float[] cosTable;
    private float[] sinTable;
    public FloatFFT(int sz){
        _sz = sz;
//        if ((sz & (sz - 1)) != 0)
  //          throw new IllegalArgumentException("sz must be a power of 2");

        cosTable = new float[_sz / 2];
        sinTable = new float[_sz / 2];
        for (int i = 0; i < _sz / 2; i++) {
            cosTable[i] = (float)(Math.cos(2 * Math.PI * i / _sz));
            sinTable[i] = (float)(Math.sin(2 * Math.PI * i / _sz));
        }
    }

    /*
     * Computes the discreteFourier transform (DFT) of the given complex vector, storing the result back into the vector.
     * The vector can have any length. This is a wrapper function.
     */
    public void transform(float[] real, float[] imag) {
        int n = real.length;
        if (n != imag.length)
            throw new IllegalArgumentException("Mismatched lengths");
        else if ((n & (n - 1)) == 0)  // Is power of 2
            transformRadix2(real, imag);
        else  // More complicated algorithm for arbitrary sizes
            //transformBluestein(real, imag);
            throw new IllegalArgumentException("must be power of 2");
    }

    /*
     * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
     * The vector can have any length. This requires the convolution function, which in turn requires the radix-2 FFT function.
     * Uses Bluestein's chirp z-transform algorithm.
     *
     * don't use this!
     */
    private void transformBluestein(float[] real, float[] imag) {
        // Find a power-of-2 convolution length m such that m >= n * 2 + 1
        int n = real.length;
        if (n != imag.length)
            throw new IllegalArgumentException("Mismatched lengths");
        if (n >= 0x20000000)
            throw new IllegalArgumentException("Array too large");
        int m = Integer.highestOneBit(n) * 4;

        // Trignometric tables
        float[] cosTable = new float[n];
        float[] sinTable = new float[n];
        for (int i = 0; i < n; i++) {
            int j = (int)((long)i * i % (n * 2));  // This is more accurate than j = i * i
            cosTable[i] = (float)Math.cos(Math.PI * j / n);
            sinTable[i] = (float)Math.sin(Math.PI * j / n);
        }

        // Temporary vectors and preprocessing
        float[] areal = new float[m];
        float[] aimag = new float[m];
        for (int i = 0; i < n; i++) {
            areal[i] =  (float)(real[i] * cosTable[i] + imag[i] * sinTable[i]);
            aimag[i] = (float)(-real[i] * sinTable[i] + imag[i] * cosTable[i]);
        }
        float[] breal = new float[m];
        float[] bimag = new float[m];
        breal[0] = (float)cosTable[0];
        bimag[0] = (float)sinTable[0];
        for (int i = 1; i < n; i++) {
            breal[i] = breal[m - i] = (float)cosTable[i];
            bimag[i] = bimag[m - i] = (float)sinTable[i];
        }

        // Convolution
        float[] creal = new float[m];
        float[] cimag = new float[m];
        convolve(areal, aimag, breal, bimag, creal, cimag);

        // Postprocessing
        for (int i = 0; i < n; i++) {
            real[i] =  creal[i] * cosTable[i] + cimag[i] * sinTable[i];
            imag[i] = -creal[i] * sinTable[i] + cimag[i] * cosTable[i];
        }
    }

    /*
     * Computes the inverse discrete Fourier transform (IDFT) of the given complex vector, storing the result back into the vector.
     * The vector can have any length. This is a wrapper function. This transform does not perform scaling, so the inverse is not a true inverse.
     */
    public void inverseTransform(float[] real, float[] imag) {
        transform(imag, real);
    }

    /*
     * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
     * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
     */
    private void transformRadix2(float[] real, float[] imag) {
        // Length variables
        int n = real.length;
        int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
        if (1 << levels != n)
            throw new IllegalArgumentException("Length is not a power of 2");

        // Bit-reversed addressing permutation
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - levels);
            if (j > i) {
                float temp = real[i];
                real[i] = real[j];
                real[j] = temp;
                temp = imag[i];
                imag[i] = imag[j];
                imag[j] = temp;
            }
        }

        // Cooley-Tukey decimation-in-time radix-2 FloatFFT
        for (int size = 2; size <= n; size *= 2) {
            int halfsize = size / 2;
            int tablestep = n / size;
            for (int i = 0; i < n; i += size) {
                for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
                    int l = j + halfsize;
                    float tpre =  real[l] * cosTable[k] + imag[l] * sinTable[k];
                    float tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k];
                    real[l] = real[j] - tpre;
                    imag[l] = imag[j] - tpim;
                    real[j] += tpre;
                    imag[j] += tpim;
                }
            }
            if (size == n)  // Prevent overflow in 'size *= 2'
                break;
        }
    }

    /*
     * Computes the circular convolution of the given complex vectors. Each vector's length must be the same.
     */
    public void convolve(float[] xreal, float[] ximag,
                                float[] yreal, float[] yimag, float[] outreal, float[] outimag) {

        int n = xreal.length;
        if (n != ximag.length || n != yreal.length || n != yimag.length
                || n != outreal.length || n != outimag.length)
            throw new IllegalArgumentException("Mismatched lengths");

        xreal = xreal.clone();
        ximag = ximag.clone();
        yreal = yreal.clone();
        yimag = yimag.clone();
        transform(xreal, ximag);
        transform(yreal, yimag);

        for (int i = 0; i < n; i++) {
            float temp = xreal[i] * yreal[i] - ximag[i] * yimag[i];
            ximag[i] = ximag[i] * yreal[i] + xreal[i] * yimag[i];
            xreal[i] = temp;
        }
        inverseTransform(xreal, ximag);

        for (int i = 0; i < n; i++) {  // Scaling (because this FloatFFT implementation omits it)
            outreal[i] = xreal[i] / n;
            outimag[i] = ximag[i] / n;
        }
    }
}