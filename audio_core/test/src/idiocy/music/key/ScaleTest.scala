package idiocy.music.key

import utest._

object ScaleTest extends TestSuite{
  val tests = Tests{
    'test1 - {
      val m = Scale.Major
      assert(m.notes.length == 7)
      assert(m.notes(0) == 0)
      assert(m.notes(1) == 2)
      assert(m.notes(2) == 4)
      assert(m.notes(3) == 5)
      assert(m.notes(4) == 7)
      assert(m.notes(5) == 9)
      assert(m.notes(6) == 11)
    }
  }
}
