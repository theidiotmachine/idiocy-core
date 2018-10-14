package idiocy.ui.renderer

import java.awt.{Dimension, Graphics, Point}

import idiocy.music.key.Key
import idiocy.ui.data.TimeSig
import idiocy.ui.utils.ArrayUtils
import idiocy.ui._
import idiocy.ui.clipboard._

import scala.collection.mutable.ArrayBuffer

final class DisplaySystem(val staffs: Array[DisplayStaff]) {



  private [this] def pasteInsertMeasures(cursor: Cursor,
                                         clipboardContentsMeasures: ClipboardContentsMeasures
                                        ): (DisplaySystem, Cursor) = {
???
  }

  private [this] def pasteOverwriteMeasuresEventSelection(selection: EventSelection,
                                                          clipboardContentsMeasures: ClipboardContentsMeasures
                                                         ): (DisplaySystem, Cursor, Selection) = {
???
  }

  private [this] def pasteOverwriteMeasuresMeasureSelection(selection: MeasureSelection,
                                                            clipboardContentsMeasures: ClipboardContentsMeasures
                                                           ): (DisplaySystem, Cursor, Selection) = {
???
  }

  private [this] def pasteInsertEvents(cursor: Cursor,
                                       clipboardContentsEvents: ClipboardContentsEvents
                                      ): (DisplaySystem, Cursor) = {
???
  }

  private [this] def pasteOverwriteEventsEventSelection(selection: EventSelection,
                                                        clipboardContentsEvents: ClipboardContentsEvents
                                                       ): (DisplaySystem, Cursor, Selection) = {
???
  }

  private [this] def pasteOverwriteEventsMeasureSelection(selection: MeasureSelection,
                                                          clipboardContentsEvents: ClipboardContentsEvents
                                                         ): (DisplaySystem, Cursor, Selection) = {
???
  }

  def pasteFromClipboardOverMeasureSelection(cursor: Cursor,
                                             measureSelection: MeasureSelection,
                                             clipboard: InternalClipboard
                                            ): (DisplaySystem, Cursor, Selection) = {
    clipboard.contents match{
      case _: ClipboardContentsEmpty =>
        (this, cursor, measureSelection)

      case clipboardContentsEvents: ClipboardContentsEvents =>
        pasteOverwriteEventsMeasureSelection(measureSelection, clipboardContentsEvents)

      case clipboardContentsMeasures: ClipboardContentsMeasures =>
        val numSelectedStaffs = (measureSelection.staffB - measureSelection.staffT) + 1
        if(numSelectedStaffs >= clipboardContentsMeasures.data.length)
          pasteOverwriteMeasuresMeasureSelection(measureSelection, clipboardContentsMeasures)
        else
          (this, cursor, measureSelection)
    }
  }

  def pasteFromClipboardOverEventSelection(cursor: Cursor,
                                           eventSelection: EventSelection,
                                           clipboard: InternalClipboard
                                          ): (DisplaySystem, Cursor, Selection) = {
    clipboard.contents match{
      case _: ClipboardContentsEmpty =>
        (this, cursor, eventSelection)

      case clipboardContentsEvents: ClipboardContentsEvents =>
        pasteOverwriteEventsEventSelection(eventSelection, clipboardContentsEvents)

      case clipboardContentsMeasures: ClipboardContentsMeasures =>
        val numClipboardStaffs = clipboardContentsMeasures.data.length
        if(numClipboardStaffs == 1)
          pasteOverwriteMeasuresEventSelection(eventSelection, clipboardContentsMeasures)
        else
          (this, cursor, eventSelection)
    }
  }

  def pasteFromClipboardAtCursor(cursor: Cursor, clipboard: InternalClipboard): (DisplaySystem, Cursor) = {
    clipboard.contents match{
      case _: ClipboardContentsEmpty =>
        (this, cursor)

      case clipboardContentsEvents: ClipboardContentsEvents =>
        pasteInsertEvents(cursor, clipboardContentsEvents)

      case clipboardContentsMeasures: ClipboardContentsMeasures =>
        val staffId = cursor.staff
        val numClipboardStaffs = clipboardContentsMeasures.data.length
        if (staffId + numClipboardStaffs < staffs.length)
          pasteInsertMeasures(cursor, clipboardContentsMeasures)
        else
          (this, cursor)
    }
  }



  def copyToClipboard(selection: Selection): InternalClipboard = {
    selection match{
      case _: NoSelection =>
        InternalClipboard()
      case eventSelection: EventSelection =>
        InternalClipboard(ClipboardContentsEvents(getClipboardMeasuresFromSelection(eventSelection)))
      case measureSelection: MeasureSelection =>
        InternalClipboard(ClipboardContentsMeasures(getClipboardMultiMeasuresFromSelection(measureSelection)))
    }
  }

  private [this] def getClipboardMeasuresFromSelection(selection: EventSelection): Array[ClipboardMeasure] = {
    staffs(selection.staff).getClipboardMeasuresFromSelection(selection)
  }

  private [this] def getClipboardMultiMeasuresFromSelection(
                                                             measureSelection: MeasureSelection
                                                           ): Array[Array[ClipboardMeasure]] = {
    val firstStaffId = measureSelection.staffT
    val lastStaffId = measureSelection.staffB
    var staffId = firstStaffId
    val out = ArrayBuffer[Array[ClipboardMeasure]]()
    while(staffId <= lastStaffId){
      out += staffs(staffId).getClipboardMeasuresFromSelection(measureSelection)
      staffId += 1
    }
    out.toArray
  }

  def insertMeasures(staffId: Int, measureId: Int, numMeasures: Int): DisplaySystem = {
    val staff = staffs(staffId)
    val newStaff = staff.insertMeasures(measureId, numMeasures)
    val newStaffs = ArrayUtils.replaceElem(staffs, staffId, newStaff)
    new DisplaySystem(newStaffs)
  }

  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplaySystem, Cursor) = {
    val staff = staffs(cursor.staff)
    val (newStaff, newCursor) = staff.insertMeasuresAtCursor(cursor, numMeasures)
    val newStaffs = ArrayUtils.replaceElem(staffs, cursor.staff, newStaff)
    (new DisplaySystem(newStaffs), newCursor)
  }

  def deleteMeasures(staffId: Int, measureId: Int, numMeasures: Int): DisplaySystem = {
    val staff = staffs(staffId)
    val newStaff = staff.deleteMeasures(measureId, numMeasures)
    val newStaffs = ArrayUtils.replaceElem(staffs, staffId, newStaff)
    new DisplaySystem(newStaffs)
  }

  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (DisplaySystem, Cursor) = {
    val staff = staffs(cursor.staff)
    val (newStaff, newCursor) = staff.deleteMeasuresAtCursor(cursor, numMeasures)
    val newStaffs = ArrayUtils.replaceElem(staffs, cursor.staff, newStaff)
    (new DisplaySystem(newStaffs), newCursor)
  }

  def backspaceAtCursor(cursor: Cursor): (DisplaySystem, Cursor) = {
    val staff = staffs(cursor.staff)
    val (newStaff, newCursor) = staff.backspaceAtCursor(cursor)
    val newStaffs = ArrayUtils.replaceElem(staffs, cursor.staff, newStaff)
    (new DisplaySystem(newStaffs), newCursor)
  }

  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (DisplaySystem, Cursor) = {
    val staff = staffs(cursor.staff)
    val (newStaff, newCursor) = staff.insertRestAtCursor(lengthPips, cursor)
    val newStaffs = ArrayUtils.replaceElem(staffs, cursor.staff, newStaff)
    (new DisplaySystem(newStaffs), newCursor)
  }

  private [this] def nextStaffId(staffId: Int, displayParams: PieceDisplayParams): Option[Int] = {
    var out = staffId + 1
    var looking = true
    while (looking && out < staffs.length) {
      if (displayParams.trackVisibility(out))
        looking = false
      else
        out += 1
    }
    if(looking)
      None
    else
      Some(out)
  }

  private [this] def prevStaffId(staffId: Int, displayParams: PieceDisplayParams): Option[Int] = {
    var out = staffId - 1
    var looking = true
    while (looking && out >= 0) {
      if (displayParams.trackVisibility(out))
        looking = false
      else
        out -= 1
    }

    if(looking)
      None
    else
      Some(out)
  }

  private [this] def nextRowStaffAndMeasureId(measureId: Int, displayParams: PieceDisplayParams): Option[(Int, Int)] = {
    val outMeasureId = measureId + displayParams.numMeasuresPerLine
    var outStaffId = 0
    var looking = true
    while (looking && outStaffId < staffs.length) {
      if(displayParams.trackVisibility(outStaffId) && outMeasureId < staffs(outStaffId).numMeasures)
        looking = false
      else
        outStaffId += 1
    }
    if(looking)
      None
    else
      Some(outStaffId, outMeasureId)
  }

  private [this] def prevRowStaffAndMeasureId(measureId: Int, displayParams: PieceDisplayParams): Option[(Int, Int)] = {
    val outMeasureId = measureId - displayParams.numMeasuresPerLine
    if(outMeasureId < 0) {
      None
    } else{
      var outStaffId = staffs.length - 1
      var looking = true
      while(looking && outStaffId >= 0){
        if (displayParams.trackVisibility(outStaffId))
          looking = false
        else
          outStaffId -= 1
      }
      if(looking)
        None
      else
        Some(outStaffId, outMeasureId)
    }
  }

  def cursorMove(cursor: Cursor, direction: Int, displayParams: PieceDisplayParams): Cursor = {
    val oCursor = staffs(cursor.staff).cursorMove(cursor, direction)
    if(oCursor.isDefined){
      oCursor.get
    } else {
      direction match {
        case Cursor.DOWN =>
          val oStaffId = nextStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined){
            val staffId = oStaffId.get
            staffs(staffId).getTopCursor(staffId, cursor.measure, cursor.event)
          }
          else {
            val oThings = nextRowStaffAndMeasureId(cursor.measure, displayParams)
            if(oThings.isDefined){
              val (staffId, measureId) = oThings.get
              staffs(staffId).getTopCursor(staffId, measureId, cursor.event)
            } else
              cursor
          }

        case Cursor.CONTROL_DOWN =>
          val oStaffId = nextStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined) {
            val staffId = oStaffId.get
            staffs(staffId).getConstrainedCursor(staffId, cursor.measure, cursor.event, cursor.barLine)
          } else {
            val oThings = nextRowStaffAndMeasureId(cursor.measure, displayParams)
            if(oThings.isDefined){
              val (staffId, measureId) = oThings.get
              staffs(staffId).getConstrainedCursor(staffId, measureId, cursor.event, cursor.barLine)
            } else
              cursor
          }

        case Cursor.UP =>
          val oStaffId = prevStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined && oStaffId.get >= 0)
            staffs(oStaffId.get).getBottomCursor(oStaffId.get, cursor.measure, cursor.event)
          else {
            val oThings = prevRowStaffAndMeasureId(cursor.measure, displayParams)
            if(oThings.isDefined){
              val (staffId, measureId) = oThings.get
              staffs(staffId).getBottomCursor(staffId, measureId, cursor.event)
            } else
              cursor
          }

        case Cursor.CONTROL_UP =>
          val oStaffId = prevStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined && oStaffId.get >= 0)
            staffs(oStaffId.get).getConstrainedCursor(oStaffId.get, cursor.measure, cursor.event, cursor.barLine)
          else {
            val oThings = prevRowStaffAndMeasureId(cursor.measure, displayParams)
            if (oThings.isDefined) {
              val (staffId, measureId) = oThings.get
              staffs(staffId).getConstrainedCursor(staffId, measureId, cursor.event, cursor.barLine)
            } else
              cursor
          }

      }
    }
  }

  def selectionMove(cursor: Cursor, selection: Selection, direction: Int, pieceDisplayParams: PieceDisplayParams): (Cursor, Selection) = {
    val newCursor = cursorMove(cursor,  direction, pieceDisplayParams)
    selection match {
      case _: NoSelection =>
        if(newCursor.staff == cursor.staff){
          val (leftCursor: Cursor, rightCursor: Cursor) = hOrderCursors(cursor, newCursor)
          (newCursor, EventSelection(cursor.staff,
            leftCursor.measure, leftCursor.event,
            rightCursor.measure, rightCursor.event
          ))
        }
        else
          (newCursor, MeasureSelection(math.min(cursor.staff, newCursor.staff),
            math.min(cursor.measure, newCursor.measure),
            math.max(cursor.staff, newCursor.staff),
            math.max(cursor.staff, newCursor.measure)))

      case measureSelection: MeasureSelection =>
        direction match{
          case Cursor.LEFT | Cursor.RIGHT | Cursor.CONTROL_LEFT | Cursor.CONTROL_RIGHT =>
            if(newCursor.measure < measureSelection.measureL)
              (staffs(newCursor.staff).getLeftCursor(newCursor.staff, newCursor.measure, newCursor.barLine),
                MeasureSelection(measureSelection.staffT, newCursor.measure, measureSelection.staffB, measureSelection.measureR))
            else if(newCursor.measure > measureSelection.measureR)
              (staffs(newCursor.staff).getRightCursor(newCursor.staff, newCursor.measure, newCursor.barLine),
                MeasureSelection(measureSelection.staffT, measureSelection.measureL, measureSelection.staffB, newCursor.measure))
            else if(direction == Cursor.LEFT || direction ==  Cursor.CONTROL_LEFT)
              (staffs(newCursor.staff).getLeftCursor(newCursor.staff, newCursor.measure, newCursor.barLine),
                MeasureSelection(measureSelection.staffT, measureSelection.measureL, measureSelection.staffB, newCursor.measure))
            else
              (staffs(newCursor.staff).getRightCursor(newCursor.staff, newCursor.measure, newCursor.barLine),
                MeasureSelection(measureSelection.staffT, newCursor.measure, measureSelection.staffB, measureSelection.measureR))
          case Cursor.UP | Cursor.DOWN =>
            if(newCursor.measure < measureSelection.measureL || newCursor.staff < measureSelection.staffT){
              val lStaffId = math.min(newCursor.staff, measureSelection.staffT)
              val rStaffId = math.max(newCursor.staff, measureSelection.staffT)
              val topLeftCursor = staffs(lStaffId).getTopLeftCursor(lStaffId, newCursor.measure)
              (topLeftCursor, MeasureSelection(lStaffId, topLeftCursor.measure, rStaffId, measureSelection.measureR))
            } else if (newCursor.measure > measureSelection.measureR || newCursor.staff > measureSelection.staffB) {
              val lStaffId = math.min(newCursor.staff, measureSelection.staffT)
              val rStaffId = math.max(newCursor.staff, measureSelection.staffT)
              val bottomRightCursor = staffs(lStaffId).getBottomRightCursor(lStaffId, newCursor.measure)
              (bottomRightCursor, MeasureSelection(lStaffId, measureSelection.measureL, rStaffId, bottomRightCursor.measure))
            } else{
              //???
              (newCursor, selection)
            }
        }

      case eventSelection: EventSelection =>
        direction match {
          case Cursor.LEFT | Cursor.RIGHT | Cursor.CONTROL_LEFT | Cursor.CONTROL_RIGHT =>
            if(newCursor.measure < eventSelection.measureL ||
              (newCursor.measure == eventSelection.measureL && newCursor.event < eventSelection.eventL))
              (newCursor, EventSelection(eventSelection.staff, newCursor.measure, newCursor.event,
                eventSelection.measureR, eventSelection.eventR
              ))
            else if(newCursor.measure > eventSelection.measureR ||
              (newCursor.measure == eventSelection.measureR && newCursor.event > eventSelection.eventR))
              (newCursor, EventSelection(eventSelection.staff, eventSelection.measureL, eventSelection.eventL,
                newCursor.measure, newCursor.event
              ))
            else if(direction == Cursor.LEFT || direction == Cursor.CONTROL_LEFT)
              (newCursor, EventSelection(eventSelection.staff, eventSelection.measureL, eventSelection.eventL,
                newCursor.measure, newCursor.event
              ))
            else
              (newCursor, EventSelection(eventSelection.staff, newCursor.measure, newCursor.event,
                eventSelection.measureR, eventSelection.eventR
              ))
          case Cursor.UP | Cursor.CONTROL_UP =>
            if(newCursor.staff != eventSelection.staff ||
              newCursor.measure < eventSelection.measureL ||
              (newCursor.measure == eventSelection.measureL && newCursor.event < eventSelection.eventL)) {
              val lStaffId = math.min(newCursor.staff, eventSelection.staff)
              val rStaffId = math.max(newCursor.staff, eventSelection.staff)
              val topLeftCursor = staffs(newCursor.staff).getTopLeftCursor(newCursor.staff, newCursor.measure)
              (topLeftCursor, MeasureSelection(lStaffId, topLeftCursor.measure, rStaffId, eventSelection.measureR))
            } else
              (newCursor, eventSelection)

          case Cursor.DOWN | Cursor.CONTROL_DOWN =>
            if(newCursor.staff != eventSelection.staff ||
              newCursor.measure > eventSelection.measureR ||
              (newCursor.measure == eventSelection.measureR && newCursor.event > eventSelection.eventR)){
              val lStaffId = math.min(newCursor.staff, eventSelection.staff)
              val rStaffId = math.max(newCursor.staff, eventSelection.staff)
              val bottomRightCursor = staffs(lStaffId).getBottomRightCursor(lStaffId, newCursor.measure)
              (bottomRightCursor, MeasureSelection(lStaffId, eventSelection.measureL, rStaffId, bottomRightCursor.measure))
            } else
              (newCursor, eventSelection)
        }
    }
  }

  private [this] def hOrderCursors(cursor: Cursor, newCursor: Cursor): (Cursor, Cursor) = {
    val (leftCursor, rightCursor) = if (newCursor.measure < cursor.measure)
      (newCursor, cursor)
    else if (cursor.measure < newCursor.measure)
      (cursor, newCursor)
    else {
      if (newCursor.event < cursor.event)
        (newCursor, cursor)
      else
        (cursor, newCursor)
    }
    (leftCursor, rightCursor)
  }

  def appendNewGrandStaff(trackId: Int): DisplaySystem = {
    new DisplaySystem(staffs :+ new DisplayGrandStaff(Array(
      new DisplayGrandStaffMeasure(TimeSig.timeSig44, 120, Key.CMajor, Array())
    )))
  }

  def render(graphics: Graphics, offset: Point, canvas: Dimension,
             firstMeasure: Int, numMeasuresPerLine: Int, numLines: Int,
             cursor: Cursor, selection: Selection,
             pieceDisplayParams: PieceDisplayParams): Unit = {

    //need to do the fancy thing on the left
    var line = 0
    var yOffs = 0
    var thisFirstMeasure = firstMeasure
    while(line < numLines) {
      val h = heightPixels(pieceDisplayParams)
      graphics.setColor(GlobalUISettings.palette.staffColor)
      graphics.drawLine(offset.x, offset.y + yOffs, offset.x, offset.y + h + yOffs)

      var i = 0
      while (i < staffs.length) {
        if(pieceDisplayParams.trackVisibility(i)) {
          val staff = staffs(i)
          val yHeight = staff.heightPixels(pieceDisplayParams)
          staff.render(graphics, new Point(offset.x, offset.y + yOffs), new Dimension(canvas.width, yHeight),
            thisFirstMeasure, numMeasuresPerLine, i, cursor, selection, pieceDisplayParams)
          yOffs += yHeight
          yOffs += pieceDisplayParams.whiteSpaceBetweenStaffsPixels
        }
        i += 1
      }
      thisFirstMeasure += numMeasuresPerLine
      line += 1
    }
  }

  def numMeasures: Int = staffs.foldLeft(0)((e, t)=>math.max(e, t.numMeasures))

  def insertNoteAtCursor(lengthPips: Int,
                         noteIdModifier: Int,
                         cursor: Cursor): (DisplaySystem, Cursor) = {
    val staff = staffs(cursor.staff)
    val (newStaff, newCursor) = staff.insertNoteAtCursor(lengthPips, noteIdModifier, cursor)
    val newStaffs = ArrayUtils.replaceElem(staffs, cursor.staff, newStaff)
    (new DisplaySystem(newStaffs), newCursor)
  }

  def heightPixels(pieceDisplayParams: PieceDisplayParams): Int = {
    var numVisible = 0
    var out = 0
    var i = 0
    while(i < staffs.length){
      if(pieceDisplayParams.trackVisibility(i)){
        out += staffs(i).heightPixels(pieceDisplayParams)
        numVisible += 1
      }
      i += 1
    }

    out += pieceDisplayParams.whiteSpaceBetweenStaffLinesPixels * (numVisible - 1)

    out
  }

  def isEmpty: Boolean = staffs.isEmpty
}
