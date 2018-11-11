package idiocy.ui.component

import java.awt.event.{KeyEvent, KeyListener, MouseEvent, MouseListener}
import java.awt.{Color, Dimension, Graphics}

import idiocy.ui.{Cursor, Document, GlobalUISettings}
import javax.swing.JComponent

class MusicComponent(document: Document) extends JComponent with MouseListener{
  override def mouseClicked(mouseEvent: MouseEvent): Unit = {
    requestFocusInWindow()
  }

  override def mousePressed(mouseEvent: MouseEvent): Unit = {}

  override def mouseReleased(mouseEvent: MouseEvent): Unit = {}

  override def mouseEntered(mouseEvent: MouseEvent): Unit = {}

  override def mouseExited(mouseEvent: MouseEvent): Unit = {}

  override def paintComponent(graphics: Graphics): Unit = {
    graphics.setColor(GlobalUISettings.palette.bgColor)
    graphics.fillRect(0, 0, getWidth, getHeight)

    document.render(graphics,
      new Dimension(getWidth, getHeight), document.selection, document.displayParams)
  }

  override def isFocusable: Boolean = true


}
