package idiocy.ui.clipboard

object ClipboardMeasure {
  val MEASURE_TYPE_FULL = 0
  val MEASURE_TYPE_STUB = 1
}

class ClipboardMeasure(val eventSets: Array[ClipboardEventSet], val beginType: Int, val endType: Int) {

}
