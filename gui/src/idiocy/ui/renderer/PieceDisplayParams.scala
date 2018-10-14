package idiocy.ui.renderer

import scala.collection.mutable.ArrayBuffer

object PieceDisplayParams{
  val TrackTypeGrandStaff = 0
  val TrackTypeTrebleStaff = 1
  val TrackTypeBassStaff = 2
  val TrackTypeAltoStaff = 3
}

class PieceDisplayParams(val trackVisibility: ArrayBuffer[Boolean] = ArrayBuffer(),
                         var showAsTraditionalKeySig: Boolean = true) {
  var numMeasuresPerLine = 0

  def staffLineSeparationPixels: Int = (staffLinePix1 * vZoom).toInt
  def staffLineWidthPixels: Int = 1
  def whiteSpaceBetweenStaffsPixels: Int = (whiteSpaceBetweenStaffsPix1 * vZoom).toInt
  def whiteSpaceBetweenStaffLinesPixels: Int = (whiteSpaceBetweenStaffLinesPix1 * vZoom).toInt
  def maxMeasureWidthPixels: Float = maxMeasureWidthPix1 * hZoom
  def clefWidthPixels: Int = (clefWidthPixels1 * hZoom).toInt

  var vZoom = 1.0f
  var hZoom = 1.0f

  private [this] val staffLinePix1 = 8.0f
  private [this] val whiteSpaceBetweenStaffsPix1 = 32.0f
  private [this] val whiteSpaceBetweenStaffLinesPix1 = 32.0f
  private [this] val maxMeasureWidthPix1 = 256.0f
  private [this] val clefWidthPixels1 = 16.0f

  val borderSizePix = 32
}
