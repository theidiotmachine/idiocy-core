package idiocy.ui.music

import java.awt.{Dimension, Graphics, Point}

import idiocy.ui.clipboard.{ClipboardContentsEvents, ClipboardContentsMeasures, ClipboardEventSet, InternalClipboard}
import idiocy.ui._
import idiocy.ui.renderer.PieceDisplayParams
import idiocy.ui.utils.ArrayUtils

class MusicSystem(val staffs: Array[Staff]) {


  private [this] def getStaff(cursor: Cursor): Staff = {
    if(cursor.staff >= staffs.length)
      staffs.last
    else
      staffs(cursor.staff)
  }

  private [this] def firstStaffId(displayParams: PieceDisplayParams): Option[Int] = {
    var out = 0
    var looking = true
    while (looking && out < staffs.length) {
      if(displayParams.trackVisibility(out))
        looking = false
      else
        out += 1
    }
    if(looking)
      None
    else
      Some(out)
  }

  private [this] def lastStaffId(displayParams: PieceDisplayParams): Option[Int] = {
    var out = staffs.length - 1
    var looking = true
    while(looking && out >= 0){
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

  /*
  private [this] def nextRowStaffAndEventId(cursor: Cursor,
                                            numMeasuresPerLine: Int,
                                            displayParams: PieceDisplayParams): Option[(Int, Int)] = {
    val oOutStaffId = firstStaffId(displayParams)
    ???
    /*
    oOutStaffId.flatMap(outStaffId=>
      staffs(cursor.staff).cursorMoveMeasuresHorizontally(cursor, numMeasuresPerLine).map(c=>(c.event, outStaffId)))
      */
  }

  private [this] def prevRowStaffAndEventId(cursor: Cursor,
                                            numMeasuresPerLine: Int,
                                            displayParams: PieceDisplayParams): Option[(Int, Int)] = {
    val oOutStaffId = lastStaffId(displayParams)
    ???
    /*
    oOutStaffId.flatMap(outStaffId=>
      staffs(cursor.staff).cursorMoveMeasuresHorizontally(cursor, -numMeasuresPerLine).map(c=>(c.event, outStaffId)))
      */
  }*/

  /*
  def cursorMove(cursor: Cursor, direction: Int, numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Option[Cursor] = {
    val staff = getStaff(cursor)
    val oNewCursor = staff.cursorMove(cursor, direction)
    if(oNewCursor.isDefined)
      oNewCursor
    else {
      direction match {
        case Cursor.LEFT | Cursor.RIGHT | Cursor.CONTROL_LEFT | Cursor.CONTROL_RIGHT => None
        case Cursor.DOWN =>
          val oStaffId = nextStaffId(cursor.staff, displayParams)
          if (oStaffId.isDefined) {
            val staffId = oStaffId.get
            Some(staffs(staffId).getTopCursor(cursor.sectionIds, staffId, cursor.event))
          }
          else {
            val oThings = nextRowStaffAndEventId(cursor, numMeasuresPerLine, displayParams)
            if (oThings.isDefined) {
              val (staffId, eventId) = oThings.get
              Some(staffs(staffId).getTopCursor(cursor.sectionIds, staffId, eventId))
            } else
              Some(cursor)
          }
        case Cursor.UP =>
          val oStaffId = prevStaffId(cursor.staff, displayParams)
          if (oStaffId.isDefined) {
            val staffId = oStaffId.get
            Some(staffs(staffId).getBottomCursor(cursor.sectionIds, staffId, cursor.event))
          }
          else {
            val oThings = prevRowStaffAndEventId(cursor, numMeasuresPerLine, displayParams)
            if (oThings.isDefined) {
              val (staffId, eventId) = oThings.get
              Some(staffs(staffId).getBottomCursor(cursor.sectionIds, staffId, eventId))
            } else
              Some(cursor)
          }
      }
    }
  }

  private [this] def hOrderCursors(cursor: Cursor, newCursor: Cursor): (Cursor, Cursor) = {
    val (leftCursor, rightCursor) = {
      if (newCursor.event < cursor.event)
        (newCursor, cursor)
      else
        (cursor, newCursor)
    }
    (leftCursor, rightCursor)
  }

  def selectionMove(cursor: Cursor,
                    selection: Selection,
                    direction: Int,
                    numMeasuresPerLine: Int,
                    pieceDisplayParams: PieceDisplayParams): Option[(Cursor, Selection)] = {
    val oNewCursor = cursorMove(cursor, direction, numMeasuresPerLine, pieceDisplayParams)
    oNewCursor.flatMap[(Cursor, Selection)](newCursor=>{
      selection match {
        case _: NoSelection =>
          if(newCursor.staff == cursor.staff){
            val (leftCursor: Cursor, rightCursor: Cursor) = hOrderCursors(cursor, newCursor)
            Some((newCursor, EventSelection(cursor.sectionIds, cursor.staff,
              leftCursor.event,
              rightCursor.event
            )))
          }
          else
            None
          /*
            (newCursor, MeasureSelection(math.min(cursor.staff, newCursor.staff),
              math.min(cursor.measure, newCursor.measure),
              math.max(cursor.staff, newCursor.staff),
              math.max(cursor.staff, newCursor.measure)))*/
          /*
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
        }*/

        case eventSelection: EventSelection =>
          direction match {
            case Cursor.LEFT | Cursor.RIGHT | Cursor.CONTROL_LEFT | Cursor.CONTROL_RIGHT =>
              if(newCursor.event < eventSelection.eventL)
                Some(newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, newCursor.event,
                  eventSelection.eventR
                ))
              else if(newCursor.event > eventSelection.eventR)
                Some(newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, eventSelection.eventL,
                  newCursor.event
                ))
              else if(direction == Cursor.LEFT || direction == Cursor.CONTROL_LEFT)
                Some(newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, eventSelection.eventL,
                  newCursor.event
                ))
              else
                Some(newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, newCursor.event,
                  eventSelection.eventR
                ))
            case Cursor.UP | Cursor.CONTROL_UP =>
              if(newCursor.staff != eventSelection.staff ||
                newCursor.event < eventSelection.eventL) {
                val lStaffId = math.min(newCursor.staff, eventSelection.staff)
                val rStaffId = math.max(newCursor.staff, eventSelection.staff)
                val topLeftCursor = staffs(newCursor.staff).getTopLeftCursor(newCursor.staff, newCursor.measure)
                //(topLeftCursor, MeasureSelection(lStaffId, topLeftCursor.measure, rStaffId, eventSelection.measureR))
                ???
              } else
                Some(newCursor, eventSelection)

            case Cursor.DOWN | Cursor.CONTROL_DOWN =>
              if(newCursor.staff != eventSelection.staff ||
                newCursor.event > eventSelection.eventR){
                val lStaffId = math.min(newCursor.staff, eventSelection.staff)
                val rStaffId = math.max(newCursor.staff, eventSelection.staff)
                val bottomRightCursor = staffs(lStaffId).getBottomRightCursor(lStaffId, newCursor.measure)
                //(bottomRightCursor, MeasureSelection(lStaffId, eventSelection.measureL, rStaffId, bottomRightCursor.measure))
                ???
              } else
                Some(newCursor, eventSelection)
          }
      }
    })
  }
  */

  private [this] def nextRowStaffAndMeasureId(measureId: Int, numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Option[(Int, Int)] = {
    val outMeasureId = measureId + numMeasuresPerLine
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

  private [this] def prevRowStaffAndMeasureId(measureId: Int, numMeasuresPerLine: Int, displayParams: PieceDisplayParams): Option[(Int, Int)] = {
    val outMeasureId = measureId - numMeasuresPerLine
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

  def cursorMove(cursor: Cursor, direction: Int,
                 numMeasuresPerLine: Int,
                 displayParams: PieceDisplayParams): Cursor = {
    val oCursor = staffs(cursor.staff).cursorMove(cursor, direction)
    if(oCursor.isDefined){
      oCursor.get
    } else {
      direction match {
        case Cursor.DOWN =>
          val oStaffId = nextStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined){
            val staffId = oStaffId.get
            staffs(staffId).getTopCursor(cursor.sectionIds, staffId, cursor.measureEventId)
          }
          else {
            val oThings = nextRowStaffAndMeasureId(cursor.measureEventId.measureId, numMeasuresPerLine, displayParams)
            if(oThings.isDefined){
              val (staffId, measureId) = oThings.get
              staffs(staffId).getTopCursor(cursor.sectionIds, staffId,
                MeasureEventId(measureId, cursor.measureEventId.eventId))
            } else
              cursor
          }

        case Cursor.CONTROL_DOWN =>
          val oStaffId = nextStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined) {
            val staffId = oStaffId.get
            staffs(staffId).getConstrainedCursor(cursor.sectionIds, staffId, cursor.measureEventId, cursor.barLine)
          } else {
            val oThings = nextRowStaffAndMeasureId(cursor.measureEventId.measureId, numMeasuresPerLine, displayParams)
            if(oThings.isDefined){
              val (staffId, measureId) = oThings.get
              staffs(staffId).getConstrainedCursor(cursor.sectionIds, staffId,
                MeasureEventId(measureId, cursor.measureEventId.eventId), cursor.barLine)
            } else
              cursor
          }

        case Cursor.UP =>
          val oStaffId = prevStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined && oStaffId.get >= 0)
            staffs(oStaffId.get).getBottomCursor(cursor.sectionIds, oStaffId.get, cursor.measureEventId)
          else {
            val oThings = prevRowStaffAndMeasureId(cursor.measureEventId.measureId, numMeasuresPerLine, displayParams)
            if(oThings.isDefined){
              val (staffId, measureId) = oThings.get
              staffs(staffId).getBottomCursor(cursor.sectionIds, staffId,
                MeasureEventId(measureId, cursor.measureEventId.eventId))
            } else
              cursor
          }

        case Cursor.CONTROL_UP =>
          val oStaffId = prevStaffId(cursor.staff, displayParams)
          if(oStaffId.isDefined && oStaffId.get >= 0)
            staffs(oStaffId.get).getConstrainedCursor(cursor.sectionIds, oStaffId.get,
              cursor.measureEventId, cursor.barLine)
          else {
            val oThings = prevRowStaffAndMeasureId(cursor.measureEventId.measureId, numMeasuresPerLine, displayParams)
            if (oThings.isDefined) {
              val (staffId, measureId) = oThings.get
              staffs(staffId).getConstrainedCursor(cursor.sectionIds, staffId,
                MeasureEventId(measureId, cursor.measureEventId.eventId), cursor.barLine)
            } else
              cursor
          }

      }
    }
  }

  def selectionMove(cursor: Cursor, selection: Selection, direction: Int,
                    numMeasuresPerLine: Int,
                    pieceDisplayParams: PieceDisplayParams): (Cursor, Selection) = {
    val newCursor = cursorMove(cursor, direction, numMeasuresPerLine, pieceDisplayParams)
    selection match {
      case _: NoSelection =>
        if(newCursor.staff == cursor.staff){
          val (leftCursor: Cursor, rightCursor: Cursor) = hOrderCursors(cursor, newCursor)
          (newCursor, EventSelection(cursor.sectionIds, cursor.staff,
            leftCursor.measureEventId,
            rightCursor.measureEventId
          ))
        }
        else
          (newCursor, MeasureSelection(cursor.sectionIds,
            math.min(cursor.staff, newCursor.staff),
            math.min(cursor.measureId, newCursor.measureId),
            math.max(cursor.staff, newCursor.staff),
            math.max(cursor.measureId, newCursor.measureId)))

      case measureSelection: MeasureSelection =>
        direction match{
          case Cursor.LEFT | Cursor.RIGHT | Cursor.CONTROL_LEFT | Cursor.CONTROL_RIGHT =>
            if(newCursor.measureId < measureSelection.measureIdL)
              (staffs(newCursor.staff).getLeftCursor(newCursor.sectionIds, newCursor.staff,
                newCursor.measureId, newCursor.barLine),
                MeasureSelection(measureSelection.sectionIds, measureSelection.staffT,
                  newCursor.measureId, measureSelection.staffB, measureSelection.measureIdR))
            else if(newCursor.measureId > measureSelection.measureIdR)
              (staffs(newCursor.staff).getRightCursor(newCursor.sectionIds, newCursor.staff,
                newCursor.measureId, newCursor.barLine),
                MeasureSelection(measureSelection.sectionIds, measureSelection.staffT,
                  measureSelection.measureIdL, measureSelection.staffB, newCursor.measureId))
            else if(direction == Cursor.LEFT || direction ==  Cursor.CONTROL_LEFT)
              (staffs(newCursor.staff).getLeftCursor(newCursor.sectionIds, newCursor.staff,
                newCursor.measureId, newCursor.barLine),
                MeasureSelection(measureSelection.sectionIds, measureSelection.staffT,
                  measureSelection.measureIdL, measureSelection.staffB, newCursor.measureId))
            else
              (staffs(newCursor.staff).getRightCursor(newCursor.sectionIds, newCursor.staff,
                newCursor.measureId, newCursor.barLine),
                MeasureSelection(measureSelection.sectionIds, measureSelection.staffT,
                  newCursor.measureId, measureSelection.staffB, measureSelection.measureIdR))
          case Cursor.UP | Cursor.DOWN =>
            if(newCursor.measureEventId.measureId < measureSelection.measureIdL
              || newCursor.staff < measureSelection.staffT){
              val lStaffId = math.min(newCursor.staff, measureSelection.staffT)
              val rStaffId = math.max(newCursor.staff, measureSelection.staffT)
              val topLeftCursor = staffs(lStaffId).getTopLeftCursor(newCursor.sectionIds, lStaffId,
                newCursor.measureId)
              (topLeftCursor, MeasureSelection(measureSelection.sectionIds, lStaffId,
                topLeftCursor.measureEventId.measureId, rStaffId, measureSelection.measureIdR))
            } else if (newCursor.measureId > measureSelection.measureIdR
              || newCursor.staff > measureSelection.staffB) {
              val lStaffId = math.min(newCursor.staff, measureSelection.staffT)
              val rStaffId = math.max(newCursor.staff, measureSelection.staffT)
              val bottomRightCursor = staffs(lStaffId).getBottomRightCursor(newCursor.sectionIds, lStaffId,
                newCursor.measureId)
              (bottomRightCursor, MeasureSelection(measureSelection.sectionIds, lStaffId, measureSelection.measureIdL,
                rStaffId, bottomRightCursor.measureId))
            } else{
              //???
              (newCursor, selection)
            }
        }

      case eventSelection: EventSelection =>
        direction match {
          case Cursor.LEFT | Cursor.RIGHT | Cursor.CONTROL_LEFT | Cursor.CONTROL_RIGHT =>
            if(newCursor.measureId < eventSelection.measureIdL ||
              (newCursor.measureId == eventSelection.measureIdL &&
                newCursor.measureEventId.eventId < eventSelection.meidL.eventId))
              (newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, newCursor.measureEventId,
                eventSelection.meidR
              ))
            else if(newCursor.measureId > eventSelection.measureIdR ||
              (newCursor.measureId == eventSelection.measureIdR &&
                newCursor.measureEventId.eventId > eventSelection.meidR.eventId))
              (newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, eventSelection.meidL,
                newCursor.measureEventId
              ))
            else if(direction == Cursor.LEFT || direction == Cursor.CONTROL_LEFT)
              (newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, eventSelection.meidL,
                newCursor.measureEventId
              ))
            else
              (newCursor, EventSelection(eventSelection.sectionIds, eventSelection.staff, newCursor.measureEventId,
                eventSelection.meidR
              ))
          case Cursor.UP | Cursor.CONTROL_UP =>
            if(newCursor.staff != eventSelection.staff ||
              newCursor.measureId < eventSelection.measureIdL ||
              (newCursor.measureId == eventSelection.measureIdL &&
                newCursor.measureEventId.eventId < eventSelection.meidL.eventId)) {
              val lStaffId = math.min(newCursor.staff, eventSelection.staff)
              val rStaffId = math.max(newCursor.staff, eventSelection.staff)
              val topLeftCursor = staffs(newCursor.staff).getTopLeftCursor(newCursor.sectionIds, newCursor.staff,
                newCursor.measureId)
              (topLeftCursor, MeasureSelection(eventSelection.sectionIds, lStaffId, topLeftCursor.measureId,
                rStaffId, eventSelection.measureIdR))
            } else
              (newCursor, eventSelection)

          case Cursor.DOWN | Cursor.CONTROL_DOWN =>
            if(newCursor.staff != eventSelection.staff ||
              newCursor.measureId > eventSelection.measureIdR ||
              (newCursor.measureId == eventSelection.measureIdR &&
                newCursor.measureEventId.eventId > eventSelection.meidR.eventId)){
              val lStaffId = math.min(newCursor.staff, eventSelection.staff)
              val rStaffId = math.max(newCursor.staff, eventSelection.staff)
              val bottomRightCursor = staffs(lStaffId).getBottomRightCursor(newCursor.sectionIds, lStaffId,
                newCursor.measureId)
              (bottomRightCursor, MeasureSelection(eventSelection.sectionIds, lStaffId, eventSelection.measureIdL,
                rStaffId, bottomRightCursor.measureId))
            } else
              (newCursor, eventSelection)
        }
    }
  }

  private [this] def hOrderCursors(cursor: Cursor, newCursor: Cursor): (Cursor, Cursor) = {
    val (leftCursor, rightCursor) = if (newCursor.measureId < cursor.measureId)
      (newCursor, cursor)
    else if (cursor.measureId < newCursor.measureId)
      (cursor, newCursor)
    else {
      if (newCursor.measureEventId.eventId < cursor.measureEventId.eventId)
        (newCursor, cursor)
      else
        (cursor, newCursor)
    }
    (leftCursor, rightCursor)
  }

  def insertRestAtCursor(lengthPips: Int, cursor: Cursor): (MusicSystem, Cursor) =
    staffOp(staff=>staff.insertRestAtCursor(lengthPips, cursor), cursor)

  def insertNoteAtCursor(lengthPips: Int, noteIdModifier: Int, cursor: Cursor): (MusicSystem, Cursor) =
    staffOp(staff=>staff.insertNoteAtCursor(lengthPips, noteIdModifier, cursor), cursor)

  def insertMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (MusicSystem, Cursor) =
    staffOp(staff=>staff.insertMeasuresAtCursor(cursor, numMeasures), cursor)

  private [this] def staffOp(f: Staff=>(Staff, Cursor), cursor: Cursor): (MusicSystem, Cursor) = {
    val staff = getStaff(cursor)
    val (newStaff, newCursor) = f(staff)
    val newStaffs = ArrayUtils.replaceElem(staffs, cursor.staff, newStaff)
    (new MusicSystem(newStaffs), newCursor)
  }

  def deleteMeasuresAtCursor(cursor: Cursor, numMeasures: Int): (MusicSystem, Cursor) =
    staffOp(staff=>staff.deleteMeasuresAtCursor(cursor, numMeasures), cursor)

  def backspaceAtCursor(cursor: Cursor): (MusicSystem, Cursor) =
    staffOp(staff=>staff.backspaceAtCursor(cursor), cursor)

  def appendNewGrandStaff: MusicSystem = {
    new MusicSystem(staffs :+ new GrandStaff(Array(), GrandStaff.generateMeasures(Array())))
  }

  def isEmpty: Boolean = staffs.isEmpty
  def render(graphics: Graphics,
             offset: Point,
             canvas: Dimension,
             firstMeasure: Int,
             numMeasuresPerLine: Int,
             numLines: Int,
             cursor: Cursor,
             selection: Selection,
             pieceDisplayParams: PieceDisplayParams): Unit = {

    //need to do the fancy thing on the left
    var line = 0
    var yOffs = 0
    var thisFirstMeasure = firstMeasure
    while(line < numLines) {
      val h = lineHeightPixels(pieceDisplayParams)
      graphics.setColor(GlobalUISettings.palette.staffColor)
      graphics.drawLine(offset.x, offset.y + yOffs, offset.x, offset.y + h + yOffs)

      var i = 0
      while (i < staffs.length) {
        if(pieceDisplayParams.trackVisibility(i)) {
          val staff = staffs(i)
          val yHeight = staff.lineHeightPixels(pieceDisplayParams)
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

  def lineHeightPixels(pieceDisplayParams: PieceDisplayParams): Int = {
    var numVisible = 0
    var out = 0
    var i = 0
    while(i < staffs.length){
      if(pieceDisplayParams.trackVisibility(i)){
        out += staffs(i).lineHeightPixels(pieceDisplayParams)
        numVisible += 1
      }
      i += 1
    }

    out += pieceDisplayParams.whiteSpaceBetweenStaffLinesPixels * (numVisible - 1)

    out
  }

  private [this] def getClipboardEventsFromSelection(selection: EventSelection): Array[ClipboardEventSet] = {
    staffs(selection.staff).getClipboardEventsFromSelection(selection)
  }

  def copyToClipboard(selection: Selection): InternalClipboard = {
    selection match{
      case _: NoSelection =>
        InternalClipboard()
      case eventSelection: EventSelection =>
        InternalClipboard(ClipboardContentsEvents(getClipboardEventsFromSelection(eventSelection)))
      case measureSelection: MeasureSelection =>
        //InternalClipboard(ClipboardContentsMeasures(getClipboardMultiEventsFromSelection(measureSelection)))
        ???
    }
  }
}
