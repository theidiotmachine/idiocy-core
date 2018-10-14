package idiocy.dsp.core

import utest._

object BufferTest extends TestSuite{
  private def write(b: SignalBuffer, writeAmount: Int): Unit = {
    var i = 0
    var l = b.writePtr
    while(i < writeAmount) {
      b.b(l) = i
      l = b.nextIndex(l)
      i += 1
    }
    b.commitWrite(writeAmount)
  }

  private def read(b: SignalBuffer, readAmount: Int, index: Int): Unit = {
    var i = 0
    var l = b.readPtr(index)
    while(i < readAmount) {
      l = b.nextIndex(l)
      i += 1
    }
    b.commitRead(readAmount, index)
  }

  val tests = Tests {
    'test1r - {
      val sampleRate = 48000
      val e = new Engine(32.0f / sampleRate.toFloat, sampleRate)
      val b1 = new SignalBuffer(e, sampleRate)
      val b1r1Index = b1.linkTo(sampleRate)

      assert(b1.writeCapacity == 31)
      assert(b1.readCapacity(b1r1Index) == 0)
      assert(b1.readEndStubSize(0, b1r1Index) == 0)
      assert(b1.readBeginStubSize(0, 0) == 0)

      //write 10
      write(b1, 10)
      assert(b1.writeCapacity == 21)
      assert(b1.readCapacity(b1r1Index) == 10)
      assert(b1.readEndStubSize(10, b1r1Index) == 10)
      assert(b1.readBeginStubSize(10, 10) == 0)

      //read 9
      read(b1, 9, b1r1Index)
      assert(b1.writeCapacity == 30)
      assert(b1.readCapacity(b1r1Index) == 1)
      assert(b1.readEndStubSize(1, b1r1Index) == 1)
      assert(b1.readBeginStubSize(1, 1) == 0)

      //read 1
      read(b1, 1, b1r1Index)
      assert(b1.writeCapacity == 31)
      assert(b1.readCapacity(b1r1Index) == 0)
      assert(b1.readEndStubSize(0, b1r1Index) == 0)
      assert(b1.readBeginStubSize(0, 0) == 0)

      //write 31
      write(b1, 31)
      assert(b1.writeCapacity == 0)
      assert(b1.readCapacity(b1r1Index) == 31)
      assert(b1.readEndStubSize(31, b1r1Index) == 22)
      assert(b1.readBeginStubSize(22, 31) == 9)
    }

    'test2r - {
      val sampleRate = 48000
      val e = new Engine(32.0f / sampleRate.toFloat, sampleRate)
      val b1 = new SignalBuffer(e, sampleRate)
      val b1r1Index = b1.linkTo(sampleRate)
      val b1r2Index = b1.linkTo(sampleRate)

      assert(b1.writeCapacity == 31)
      assert(b1.readCapacity(b1r1Index) == 0)
      assert(b1.readCapacity(b1r2Index) == 0)

      //write 10
      write(b1, 10)
      assert(b1.writeCapacity == 21)
      assert(b1.readCapacity(b1r1Index) == 10)
      assert(b1.readCapacity(b1r2Index) == 10)

      //read 9, 1
      read(b1, 9, b1r1Index)
      assert(b1.writeCapacity == 21)
      assert(b1.readCapacity(b1r1Index) == 1)
      assert(b1.readCapacity(b1r2Index) == 10)

      //read 1, 2
      read(b1, 1, b1r2Index)
      assert(b1.writeCapacity == 22)
      assert(b1.readCapacity(b1r1Index) == 1)
      assert(b1.readCapacity(b1r2Index) == 9)

      //
      write(b1, 22)
      assert(b1.writeCapacity == 0)
      assert(b1.readCapacity(b1r1Index) == 23)
      assert(b1.readCapacity(b1r2Index) == 31)
    }
  }
}
