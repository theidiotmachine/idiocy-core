package idiocy.ui

import java.awt.Font

import idiocy.ui.clipboard.InternalClipboard
import javax.swing._

class Main extends Runnable{

  GlobalUISettings.noteFont = new Font("Sans", Font.PLAIN, 20)

  override def run(): Unit = {
    val doc = new Document
    doc.go("New Piece")
  }
}

object Main{

  var clipboard: InternalClipboard = InternalClipboard()

  def main(args: Array[String]): Unit = {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    SwingUtilities.invokeLater(new Main)
  }
}
