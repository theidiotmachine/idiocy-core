package idiocy.ui.utils

import scala.reflect.ClassTag

object ArrayUtils {
  def removeIndex[T](in: Array[T], idx: Int)(implicit m: ClassTag[T]): Array[T] = {
    val out: Array[T] = new Array[T](in.length - 1)
    Array.copy(in, 0, out, 0, idx)
    Array.copy(in, idx + 1, out, idx, in.length - (idx+1))
    out
  }

  def removeIndexes[T](in: Array[T], idx: Int, num: Int)(implicit m: ClassTag[T]): Array[T] = {
    val out: Array[T] = new Array[T](in.length - num)
    Array.copy(in, 0, out, 0, idx)
    Array.copy(in, idx + num, out, idx, in.length - (idx+num))
    out
  }

  def removeIndexes[T](in: Array[T], idxs: Array[Int])(implicit m: ClassTag[T]): Array[T] = {
    val out: Array[T] = new Array[T](in.length - idxs.length)

    var inIdx = 0
    var idxsIdx = 0
    var outIdx = 0
    while(inIdx < in.length && idxsIdx < idxs.length){
      if(inIdx == idxs(idxsIdx)){
        idxsIdx += 1
        inIdx += 1
      } else {
        out(outIdx) = in(inIdx)
        inIdx += 1
        outIdx += 1
      }
    }

    if(inIdx < in.length)
      Array.copy(in, inIdx, out, outIdx, in.length - inIdx)

    out
  }

  def findIndex[T](in: Array[T], f: T=>Boolean): Option[Int] = {
    var i = 0
    var out: Option[Int] = None
    while(i < in.length && out.isEmpty){
      if(f(in(i)))
        out = Some(i)
      i += 1
    }
    out
  }

  def insertElemAndShift[T](in: Array[T], idx: Int, elem: T)(implicit m: ClassTag[T]): Array[T] = {
    val out: Array[T] = new Array[T](in.length + 1)
    Array.copy(in, 0, out, 0, idx)
    out(idx) = elem
    Array.copy(in, idx, out, idx, in.length - idx)
    out
  }

  def insertElemsAndShift[T](in: Array[T], idx: Int, elems: Array[T])(implicit m: ClassTag[T]): Array[T] = {
    val out: Array[T] = new Array[T](in.length + elems.length)
    Array.copy(in, 0, out, 0, idx)
    Array.copy(elems, 0, out, idx, elems.length)
    Array.copy(in, idx, out, idx + elems.length, in.length - idx)
    out
  }

  def replaceElem[T](in: Array[T], idx: Int, elem: T)(implicit m: ClassTag[T]): Array[T] = {
    val out = new Array[T](in.length)
    Array.copy(in, 0, out, 0, idx)
    out(idx) = elem
    Array.copy(in, idx + 1, out, idx + 1, in.length - (idx+1))
    out
  }

  def replaceElems[T](in: Array[T], idxFrom: Int, num: Int, elems: Array[T])(implicit m: ClassTag[T]): Array[T] = {
    val idxTo = idxFrom + num
    val out = new Array[T](in.length - num + elems.length)
    Array.copy(in, 0, out, 0, idxFrom)
    Array.copy(elems, 0, out, idxFrom, elems.length)
    Array.copy(in, idxTo, out, idxFrom + elems.length, (in.length - num) - idxFrom)
    out
  }

  /**
    * Extract a sub range
    *
    * @param in the array
    * @param idxFrom inclusive
    * @param idxTo exclusive
    * @param m scala array magic
    * @tparam T array type
    * @return
    */
  def subRange[T](in: Array[T], idxFrom: Int, idxTo: Int)(implicit m: ClassTag[T]): Array[T] = {
    val out = new Array[T](idxTo - idxFrom)
    Array.copy(in, idxFrom, out, 0, idxTo - idxFrom)
    out
  }
}
