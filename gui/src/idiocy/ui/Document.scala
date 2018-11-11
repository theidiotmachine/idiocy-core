package idiocy.ui

import java.awt._
import java.awt.event.ActionEvent
import java.io.File

import idiocy.ui.action._
import idiocy.ui.component.MusicComponent
import idiocy.ui.forms.PaletteChooser
import idiocy.ui.io.idiocy.IOIdiocy
import idiocy.ui.io.musicxml.IOMusicXML
import idiocy.ui.music.{LeafSection, MusicSystem, Piece}
import idiocy.ui.renderer.PieceDisplayParams
import javax.swing._

import scala.collection.mutable.ArrayBuffer

object Document{
  val extension: String = "stpd"
}

class JavaRepaintCallback(document: Document){
  def go(): Unit = document.repaint()
}

class Document {
  private [this] def calcNumMeasuresPerLine(canvasWidth: Int,
                                            displayParams: PieceDisplayParams): Int = {
    val drawWidth = canvasWidth - displayParams.clefWidthPixels
    var found = false
    var numMeasuresPerLine = 1
    while (!found) {
      val pix = numMeasuresPerLine * (displayParams.maxMeasureWidthPixels + 1) //1 is the line between staffs
      if (pix < drawWidth) {
        numMeasuresPerLine = numMeasuresPerLine match{
          case 1 => 2
          case 2 => 4
          case _ => numMeasuresPerLine + 4
        }
      } else {
        found = true
      }
    }

    numMeasuresPerLine
  }

  //argh
  private [this] var numMeasuresPerLine = 0

  def render(graphics: Graphics, canvas: Dimension, selection: Selection, displayParams: PieceDisplayParams): Unit = {
    val offset = new Point(displayParams.borderSizePix, displayParams.borderSizePix)
    val newCanvas = new Dimension(canvas.width - displayParams.borderSizePix * 2, canvas.height - displayParams.borderSizePix)

    numMeasuresPerLine = calcNumMeasuresPerLine(newCanvas.width, displayParams)

    piece.render(graphics, offset, newCanvas, numMeasuresPerLine, cursor, selection, displayParams)
  }

  def copyToClipboard(): Unit = {
    Main.clipboard = piece.copyToClipboard(selection)
  }

  def paste(): Unit = {
    selection match {
      case _: NoSelection => doAction(new PasteAtCursor(Main.clipboard))
      case _ => doAction(new PasteOverSelection(Main.clipboard))
    }
  }

  def backspace(): Unit = {
    selection match {
      case _: NoSelection => doAction(new BackspaceAtCursor())
      case measureSelection: MeasureSelection => ???
      case eventSelection: EventSelection => ???
    }
  }

  def insertMeasure(): Unit = {
    selection match {
      case _: NoSelection => doAction(new InsertMeasureAtCursor())
      case measureSelection: MeasureSelection => ???
      case eventSelection: EventSelection => ???
    }
  }

  def clearSelection(): Unit = {
    selection = NoSelection()
    repaintCallBack()
  }

  def selectionMove(direction: Int): Unit = {
    val (newCursor, newSelection) = piece.selectionMove(cursor, selection, direction, numMeasuresPerLine, displayParams)
    selection = newSelection
    cursor = newCursor
    repaintCallBack()
  }

  val displayParams: PieceDisplayParams = new PieceDisplayParams()

  //new
  var piece: Piece = new Piece(new LeafSection("Section 1", new MusicSystem(Array())))
  var cursor: Cursor = Cursor.emptyCursor
  var selection: Selection = NoSelection()

  var fName: String = ""
  var dirName: String = ""

  def cursorMove(direction: Int): Unit = {
    val newCursor = piece.cursorMove(cursor, direction, numMeasuresPerLine, displayParams)
    selection = NoSelection()

    cursor = newCursor
    repaintCallBack()
  }

  val undoStack: ArrayBuffer[UserAction] = new ArrayBuffer[UserAction]()
  val redoStack: ArrayBuffer[UserAction] = new ArrayBuffer[UserAction]()

  var repaintCallBack: () => Unit = ()=>{}
  def repaint(): Unit = repaintCallBack()

  def doAction(action: UserAction): Unit = {
    undoStack.+=(action)
    redoStack.clear()
    val actionResult = action.apply(piece, displayParams, cursor, selection)
    piece = actionResult.piece
    cursor = actionResult.cursor
    selection = actionResult.selection
    if(actionResult.redraw)
      repaintCallBack()
  }

  def undoAction(): Unit = {
    if(undoStack.nonEmpty) {
      val head = undoStack.last
      undoStack.remove(undoStack.length - 1, 1)
      redoStack += head
      val actionResult = head.undo(piece, displayParams, cursor, selection)
      piece = actionResult.piece
      cursor = actionResult.cursor
      selection = actionResult.selection
      if (actionResult.redraw)
        repaintCallBack()
    }
  }

  def redoAction(): Unit = {
    if(redoStack.nonEmpty) {
      val head = redoStack.last
      redoStack.remove(redoStack.length - 1, 1)
      undoStack += head
      val actionResult = head.apply(piece, displayParams, cursor, selection)
      piece = actionResult.piece
      cursor = actionResult.cursor
      selection = actionResult.selection
      if (actionResult.redraw)
        repaintCallBack()
    }
  }

  def importMusicXML(dirName: String, fName: String): Unit = {

    val (doc: Document, newDoc: Boolean) = docToReadInto

    IOMusicXML.read(dirName, fName, doc)

    doc.dirName = dirName
    doc.fName = ""

    if(newDoc)
      doc.go(fName)
    else{
      doc.setFrameTitle(fName)
      doc.repaintCallBack()
    }
  }

  private def docToReadInto: (Document, Boolean) = {
    val (doc, newDoc) = if (piece.isEmpty && undoStack.isEmpty && redoStack.isEmpty) {
      (this, false)
    } else {
      (new Document, true)
    }
    (doc, newDoc)
  }

  def importMusicXMLInteractive(): Unit = {
    val fd = new FileDialog(frame, "Import MusicXML")
    fd.setMode(FileDialog.LOAD)
    fd.setFilenameFilter((_: File, s: String) => s.toLowerCase.endsWith("xml"))
    fd.setVisible(true)
    val thisDirName = fd.getDirectory
    val fName = fd.getFile
    if(thisDirName != null && fName != null){

      dirName = thisDirName
      importMusicXML(dirName, fName)
    }
  }

  private [this] val keys: Keys = new Keys(this)

  private [this] var frame: JFrame = _

  var saveMenuItem: JMenuItem = _

  def setFrameTitle(in: String): Unit = frame.setTitle(in)

  private [this] def newMenuItem(menu: JMenu, name: String, callback: ActionEvent=>Unit): JMenuItem = {
    val menuItem = new JMenuItem(name)
    menuItem.addActionListener((actionEvent: ActionEvent) => callback(actionEvent))
    menu.add(menuItem)
    menuItem
  }

  private [this] def newSubMenu(menu: JMenu, name: String): JMenu = {
    val subMenu = new JMenu(name)
    menu.add(subMenu)
    subMenu
  }

  def showAll(): Unit = {
    ???
    /*
    displayParams.trackVisibility.transform(_=>true)
    repaintCallBack()
    */
  }

  def hideAllButThis(): Unit = {
    ???
    /*
    displayParams.trackVisibility.transform(_=>false)
    displayParams.trackVisibility(cursor.staff) = true
    repaintCallBack()
    */
  }

  def newDocument(): Unit = {
    val doc = new Document
    doc.go("New Piece")
  }

  def openDocumentInteractive(): Unit = {
    val fd = new FileDialog(frame, "Open")
    fd.setMode(FileDialog.LOAD)
    fd.setFilenameFilter((_: File, s: String) => s.toLowerCase.endsWith(Document.extension))
    fd.setVisible(true)
    val dirName = fd.getDirectory
    val fName = fd.getFile
    if(dirName != null && fName != null)
      openDocument(dirName, fName)
  }

  def openDocument(dirName: String, fName: String): Unit = {
    val (doc: Document, newDoc: Boolean) = docToReadInto

    IOIdiocy.readDoc(dirName, fName, doc)
    doc.dirName = dirName
    doc.fName = fName

    if(newDoc)
      doc.go(fName)
    else{
      doc.saveMenuItem.setEnabled(fName != "" && dirName != "")
      doc.setFrameTitle(fName)
      doc.repaintCallBack()
    }
  }

  def saveDocument(): Unit = {
    if(fName != "")
      IOIdiocy.writeDoc(dirName, fName, this)
  }

  def saveDocumentAs(): Unit = {
    val fd = new FileDialog(frame, "Save As")
    fd.setMode(FileDialog.SAVE)
    fd.setFilenameFilter((_: File, s: String) => s.toLowerCase.endsWith(Document.extension))
    fd.setVisible(true)

    val thisDirName = fd.getDirectory
    val thisFName = fd.getFile

    if(thisDirName != null && thisFName != null){
      dirName = thisDirName
      fName = thisFName
      saveDocument()
      saveMenuItem.setEnabled(fName != "" && dirName != "")
    }
  }

  def openPaletteDialog(): Unit = {
    val dialog = new PaletteChooser(frame, new JavaRepaintCallback(this))
    dialog.pack()
    dialog.setLocationRelativeTo(frame)
    dialog.setVisible(true)
  }

  def go(frameTitle: String): Unit = {
    val mc = new MusicComponent(this)

    frame = new JFrame(frameTitle)
    val menuBar = new JMenuBar

    val fileMenu = new JMenu("File")
    menuBar.add(fileMenu)

    newMenuItem(fileMenu, "New", (_: ActionEvent)=>newDocument())
    newMenuItem(fileMenu, "Open...", (_: ActionEvent)=>openDocumentInteractive())
    newMenuItem(fileMenu, "Palette...", (_:ActionEvent)=>openPaletteDialog())

    val importMenu = newSubMenu(fileMenu, "Import")
    newMenuItem(importMenu, "Music XML (Beta!)", (_: ActionEvent) => importMusicXMLInteractive())

    saveMenuItem = newMenuItem(fileMenu, "Save", (_: ActionEvent)=>saveDocument())
    saveMenuItem.setEnabled(fName != "" && dirName != "")
    newMenuItem(fileMenu, "Save As...", (_: ActionEvent)=>saveDocumentAs())

    val editMenu = new JMenu("Edit")
    menuBar.add(editMenu)
    newMenuItem(editMenu, "Undo", (_: ActionEvent)=>undoAction())
    newMenuItem(editMenu, "Redo", (_: ActionEvent)=>redoAction())

    val viewMenu = new JMenu("View")
    menuBar.add(viewMenu)
    val showHideMenu = newSubMenu(viewMenu, "Show/Hide")
    newMenuItem(showHideMenu, "Show All", (_:ActionEvent)=>showAll())
    newMenuItem(showHideMenu, "Hide Everything Else", (_:ActionEvent)=>hideAllButThis())

    val pieceMenu = new JMenu("Piece")
    menuBar.add(pieceMenu)
    newMenuItem(pieceMenu, "New Grand Staff", (_: ActionEvent)=>doAction(new AddGrandStaff))

    val staffMenu = new JMenu("Staff")
    menuBar.add(staffMenu)
    newMenuItem(staffMenu, "Insert Measure", (_: ActionEvent)=>doAction(new InsertMeasureAtCursor))
    newMenuItem(staffMenu, "Delete Measure", (_: ActionEvent)=>doAction(new DeleteMeasureAtCursor))

    frame.setJMenuBar(menuBar)

    mc.addKeyListener(keys)
    mc.addMouseListener(mc)
    mc.setFocusTraversalKeysEnabled(false)
    mc.requestFocus()

    repaintCallBack = ()=>mc.repaint()

    //mc.getGraphics

    frame.getContentPane.add(mc)

    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame.setPreferredSize(new Dimension(400, 300))
    frame.pack()
    frame.setLocationRelativeTo(null)
    frame.setVisible(true)
  }
}
