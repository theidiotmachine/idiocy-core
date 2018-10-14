package idiocy.ui.clipboard

sealed trait ClipboardContents

case class ClipboardContentsEmpty() extends ClipboardContents

case class ClipboardContentsEvents(data: Array[ClipboardMeasure]) extends ClipboardContents

case class ClipboardContentsMeasures(data: Array[Array[ClipboardMeasure]]) extends ClipboardContents

case class InternalClipboard(contents: ClipboardContents = ClipboardContentsEmpty()) {


  /*
hello

  import java.awt.Toolkit
  import java.awt.datatransfer.Clipboard
  import java.awt.datatransfer.StringSelection

  val selection = new StringSelection(theString)
  val clipboard: Clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
  clipboard.setContents(selection, selection)
  */


}
