package idiocy.ui.io.idiocy

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util

import idiocy.ui.data.TimeSig
import idiocy.ui.music._
import idiocy.ui.music.event._
import idiocy.ui.renderer._
import idiocy.ui.{Cursor, Document, Selection}
import ujson.Js
import upickle.default._

import scala.util.{Success, Try}

object IOIdiocy {
  private [this] def readDisplayParams(js: Js, out: PieceDisplayParams): Unit = {
    out.trackVisibility ++= upickle.default.readJs[Array[Boolean]](js("trackVisibility"))
    out.showAsTraditionalKeySig = js("showAsTraditionalKeySig").bool
    out.vZoom = js("vZoom").num.toFloat
    out.hZoom = js("hZoom").num.toFloat
  }

  private [this] def writeDisplayParams(displayParams: PieceDisplayParams): Js = {
    Js.Obj(
      ("trackVisibility", upickle.default.writeJs(displayParams.trackVisibility)),
      ("showAsTraditionalKeySig", displayParams.showAsTraditionalKeySig),
      ("vZoom", displayParams.vZoom),
      ("hZoom", displayParams.hZoom)
    )
  }

  private [this] def readEvent(js: Js): MusicEvent = {
    val typeKey = js("__type__").str
    val dataKey = js("__data__").str
    typeKey match {
      case "MusicNote" =>
        read[MusicNote](dataKey)
      case "FullRest" =>
        read[FullRest](dataKey)
      case "PartialRest" =>
        read[PartialRest](dataKey)
      case "MusicNoteSpan" =>
        read[MusicNoteSpan](dataKey)
      case "KeySignatureEvent" =>
        read[KeySignatureEvent](dataKey)
      case "TimeSigEvent" =>
        read[TimeSigEvent](dataKey)
    }
  }

  private [this] def writeEvent(displayEvent: MusicEvent): Js = {
    displayEvent match {
      case note: MusicNote =>
        val noteJs = write(note)
        Js.Obj(("__type__", "MusicNote"), ("__data__", noteJs))
      case rest: FullRest =>
        val restJs = write(rest)
        Js.Obj(("__type__", "FullRest"), ("__data__", restJs))
      case rest: PartialRest =>
        val restJs = write(rest)
        Js.Obj(("__type__", "PartialRest"), ("__data__", restJs))
      case span: MusicNoteSpan =>
        val spanJs = write(span)
        Js.Obj(("__type__", "DisplaySpan"), ("__data__", spanJs))
      case kse: KeySignatureEvent =>
        val kseJs = write(kse)
        Js.Obj(("__type__", "KeySignatureEvent"), ("__data__", kseJs))
      case tse: TimeSigEvent =>
        val tseJs = write(tse)
        Js.Obj(("__type__", "TimeSigEvent"), ("__data__", tseJs))
    }
  }

  private [this] def readEventSet(js: Js): MusicEventSet = {
    val events = js("events").arr.map(e=>readEvent(e)).toArray
    val lengthPips = js("lengthPips").num.toInt
    new MusicEventSet(events, lengthPips)
  }

  private [this] def writeEventSet(displayEventSet: MusicEventSet): Js = {
    Js.Obj(
      ("lengthPips", displayEventSet.lengthPips.toDouble),
      ("events", Js.Arr(displayEventSet.events.map(e=>writeEvent(e)):_*))
    )
  }

  private [this] def readGrandStaffMeasure(js: Js): GrandStaffMeasure = {
    val timeSig = read[TimeSig](js("timeSig").str)
    val events = js("events").arr.map(e=>readEventSet(e)).toArray
    val firstEventIndex = js("firstEventIndex").num.toInt
    new GrandStaffMeasure(timeSig, events, firstEventIndex)
  }

  private [this] def writeGrandStaffMeasure(grandStaffMeasure: GrandStaffMeasure): Js = {
    Js.Obj(
      ("timeSig", write(grandStaffMeasure.timeSig)),
      ("events", Js.Arr(grandStaffMeasure.events.map(e=>writeEventSet(e)):_*)),
      ("firstEventIndex", grandStaffMeasure.firstEventIndex)
    )
  }

  private [this] def readStaff(js: Js): Staff = {
    val typeKey = js("__type__").str
    typeKey match {
      case "GrandStaff" =>
        val measuresJs = js("measures").arr
        val measures = measuresJs.map(m=>readGrandStaffMeasure(m)).toArray
        val eventsJs = js("events").arr
        val events = eventsJs.map(e=>readEventSet(e)).toArray
        new GrandStaff(events, measures)
    }
  }

  private [this] def writeStaff(displayStaff: Staff): Js = {
    displayStaff match {
      case displayGrandStaff: GrandStaff => Js.Obj(
        ("__type__", "GrandStaff"),
        ("measures", Js.Arr(displayGrandStaff.measures.map(m=>writeGrandStaffMeasure(m)):_*)),
        ("events", Js.Arr(displayGrandStaff.events.map(e=>writeEventSet(e)):_*))
      )
    }
  }

  private [this] def readSystem(js: Js): MusicSystem = {
    val staffsJs = js("staffs").arr
    val staffs = staffsJs.map(s=>readStaff(s)).toArray
    new MusicSystem(staffs)
  }

  private [this] def writeSystem(system: MusicSystem): Js = {
    val staffs = system.staffs.map(s=>writeStaff(s))
    Js.Obj(
      ("staffs", Js.Arr(staffs:_*))
    )
  }

  private [this] def readLeafSection(js: Js): LeafSection = {
    new LeafSection(js("name").str, readSystem(js("system")))
  }

  private [this] def writeLeafSection(leafSection: LeafSection): Js = {
    Js.Obj(("name", leafSection.name), ("system", writeSystem(leafSection.system)))
  }

  private [this] def readBranchSection(js: Js): BranchSection = {
    val sectionsJs = js("sections").arr
    val sections = sectionsJs.map(s=>readSection(s)).toArray
    new BranchSection(js("name").str, sections)
  }

  private [this] def writeBranchSection(branchSection: BranchSection): Js = {
    Js.Obj(("name", branchSection.name), ("sections", Js.Arr(branchSection.sections.map(s=>writeSection(s)):_*)))
  }

  private [this] def readSection(js: Js): Section = {
    val typeKey = js("__type__").str
    val dataKey = js("__data__")
    typeKey match {
      case "LeafSection" => readLeafSection(dataKey)
      case "BranchSection" => readBranchSection(dataKey)
    }
  }

  private [this] def writeSection(section: Section): Js = {
    section match {
      case leafSection: LeafSection =>
        val lsJs = writeLeafSection(leafSection)
        Js.Obj(("__type__", "LeafSection"), ("__data__", lsJs))
      case branchSection: BranchSection =>
        val bsJs = writeBranchSection(branchSection)
        Js.Obj(("__type__", "BranchSection"), ("__data__", bsJs))
    }
  }

  private [this] def readPiece(js: Js): Piece  = {
    new Piece(readSection(js("section")))
  }

  private [this] def writePiece(piece: Piece): Js = {
    Js.Obj(
      ("section", writeSection(piece.section))
    )
  }

  private [this] def readDocument(js: Js, document: Document): Unit = {
    readDisplayParams(js("displayParams"), document.displayParams)
    document.piece = readPiece(js("piece"))
    document.cursor = read[Cursor](js("cursor").str)
    document.selection = read[Selection](js("selection").str)
  }

  private [this] def writeDocument(document: Document): Js = {
    Js.Obj(
      ("displayParams", writeDisplayParams(document.displayParams)),
      ("piece", writePiece(document.piece)),
      ("cursor", write(document.cursor)),
      ("selection", write(document.selection))
    )
  }

  def readDoc(dirName: String, fName: String, doc: Document): Unit = {
    val oEncoded = Try(Files.readAllBytes(Paths.get(dirName + "/" + fName)))
    oEncoded match  {
      case Success(encoded) =>
        val str = new String(encoded, "UTF-8")
        val js = ujson.read(str)
        readDocument(js, doc)
      case _ =>
    }
  }

  def writeDoc(dirName: String, fName: String, doc: Document): Unit = {
    val js = writeDocument(doc)
    val json = js.render()
    val f = Paths.get(dirName + "/" + fName)
    Files.write(f, util.Arrays.asList(json), Charset.forName("UTF-8"))
  }
}
