package idiocy.ui

import java.awt.event.{KeyEvent, KeyListener}

import idiocy.ui.action.{BackspaceAtCursor, InsertNoteAtCursor, InsertRestAtCursor}
import idiocy.ui.renderer.DisplayEvent

class Keys(document: Document) extends KeyListener{
  override def keyTyped(keyEvent: KeyEvent): Unit = {
    val kc = keyEvent.getKeyChar
    kc match{
      case 'q' =>
        document.doAction(new InsertNoteAtCursor(DisplayEvent.Whole, 0))
      case 'W' =>
        document.doAction(new InsertNoteAtCursor(DisplayEvent.DottedHalf, 0))
      case 'w' =>
        document.doAction(new InsertNoteAtCursor(DisplayEvent.Half, 0))
      case 'E' => document.doAction(new InsertNoteAtCursor(DisplayEvent.DottedQuarter, 0))
      case 'e' => document.doAction(new InsertNoteAtCursor(DisplayEvent.Quarter, 0))
      case 'R' => document.doAction(new InsertNoteAtCursor(DisplayEvent.DottedEighth, 0))
      case 'r' => document.doAction(new InsertNoteAtCursor(DisplayEvent.Eighth, 0))
      case 'T' => document.doAction(new InsertNoteAtCursor(DisplayEvent.DottedSixteenth, 0))
      case 't' => document.doAction(new InsertNoteAtCursor(DisplayEvent.Sixteenth, 0))
      case 'a' => document.doAction(new InsertRestAtCursor(DisplayEvent.Whole))
      case 'S' => document.doAction(new InsertRestAtCursor(DisplayEvent.DottedHalf))
      case 's' => document.doAction(new InsertRestAtCursor(DisplayEvent.Half))
      case 'D' => document.doAction(new InsertRestAtCursor(DisplayEvent.DottedQuarter))
      case 'd' => document.doAction(new InsertRestAtCursor(DisplayEvent.Quarter))
      case 'F' => document.doAction(new InsertRestAtCursor(DisplayEvent.DottedEighth))
      case 'f' => document.doAction(new InsertRestAtCursor(DisplayEvent.Eighth))
      case 'G' => document.doAction(new InsertRestAtCursor(DisplayEvent.DottedSixteenth))
      case 'g' => document.doAction(new InsertRestAtCursor(DisplayEvent.Sixteenth))
      case _ =>
    }
  }

  override def keyPressed(keyEvent: KeyEvent): Unit = {
    val kc = keyEvent.getKeyCode
    kc match {
      case KeyEvent.VK_LEFT =>
        if(keyEvent.isShiftDown)
          if(keyEvent.isControlDown)
            document.selectionMove(Cursor.CONTROL_LEFT)
          else
            document.selectionMove(Cursor.LEFT)
        else if(keyEvent.isControlDown)
          document.cursorMove(Cursor.CONTROL_LEFT)
        else
          document.cursorMove(Cursor.LEFT)
      case KeyEvent.VK_RIGHT =>
        if(keyEvent.isShiftDown)
          if(keyEvent.isControlDown)
            document.selectionMove(Cursor.CONTROL_RIGHT)
          else
            document.selectionMove(Cursor.RIGHT)
        else if(keyEvent.isControlDown)
          document.cursorMove(Cursor.CONTROL_RIGHT)
        else
          document.cursorMove(Cursor.RIGHT)
      case KeyEvent.VK_UP =>
        if(keyEvent.isShiftDown)
          if(keyEvent.isControlDown)
            document.selectionMove(Cursor.CONTROL_UP)
          else
            document.selectionMove(Cursor.UP)
        else if(keyEvent.isControlDown)
          document.cursorMove(Cursor.CONTROL_UP)
        else
          document.cursorMove(Cursor.UP)
      case KeyEvent.VK_DOWN =>
        if(keyEvent.isShiftDown)
          if(keyEvent.isControlDown)
            document.selectionMove(Cursor.CONTROL_DOWN)
        else
            document.selectionMove(Cursor.DOWN)
        else if(keyEvent.isControlDown)
          document.cursorMove(Cursor.CONTROL_DOWN)
        else
          document.cursorMove(Cursor.DOWN)
      case KeyEvent.VK_Z => if(keyEvent.isControlDown) document.undoAction()
      case KeyEvent.VK_Y => if(keyEvent.isControlDown) document.redoAction()
      case KeyEvent.VK_C => if(keyEvent.isControlDown) document.copyToClipboard()
      case KeyEvent.VK_V => if(keyEvent.isControlDown) document.paste()
      case KeyEvent.VK_BACK_SPACE => document.backspace()
      case KeyEvent.VK_ESCAPE => document.clearSelection()
      case _ =>
    }
  }

  override def keyReleased(keyEvent: KeyEvent): Unit = {}
}
