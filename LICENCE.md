# Licence information

This file contains licence information for Idiocy core. 
If you intend to use, contribute to or distribute this, please read this
document.

If you believe I have made a mistake, please contact me and I will attempt
to rectify it.

## Tarsos

Idiocy uses a small part of [Tarsos DSP](https://github.com/JorenSix/TarsosDSP), 
principally to encode and unencode audio data. These files are in the 
be.tarsos.dsp.io package. Tarsos is GPLv3, but the two files (refactored 
to three) seem to be GPLv2 and copyright Sun/Oracle, so I wonder if
they came from the JVM. As a result, the licence of these remains GPLv2.

## Project Nayuki

Idiocy uses the amazing work of [Project Nayuki](https://www.nayuki.io/) 
in two different places.

### Flac libraries

The Flac encoding and decoding libraries are in io.nayuki.flac.common, 
io.nayuki.flac.encode and io.nayuki.flac.decode. These are released under 
LGPLv3. 

### FFT library

The FFT code is released under the MIT licence. This was ported to Scala
and modified, and so is in the main idiocy code base, but the copyright
is retained in the file.

## The rest

The rest of the codebase is released under the MIT licence. However the 
presence of GPLv2 code probably means that unless you use individual bits, 
you will realistically have to treat this as GPL'ed too.