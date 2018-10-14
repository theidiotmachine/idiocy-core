package idiocy.ui.io.idiocy

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util

import idiocy.music.key.Key
import idiocy.ui.data.TimeSig
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

  private [this] def readEvent(js: Js): DisplayEvent = {
    val typeKey = js("__type__").str
    val dataKey = js("__data__").str
    typeKey match {
      case "DisplayNote" =>
        read[DisplayNote](dataKey)
      case "DisplayFullRest" =>
        read[DisplayFullRest](dataKey)
      case "DisplayPartialRest" =>
        read[DisplayPartialRest](dataKey)
      case "DisplaySpan" =>
        read[DisplaySpan](dataKey)
    }
  }

  private [this] def writeEvent(displayEvent: DisplayEvent): Js = {
    displayEvent match {
      case note: DisplayNote =>
        val noteJs = write(note)
        Js.Obj(("__type__", "DisplayNote"), ("__data__", noteJs))
      case rest: DisplayFullRest =>
        val restJs = write(rest)
        Js.Obj(("__type__", "DisplayFullRest"), ("__data__", restJs))
      case rest: DisplayPartialRest =>
        val restJs = write(rest)
        Js.Obj(("__type__", "DisplayPartialRest"), ("__data__", restJs))
      case span: DisplaySpan =>
        val spanJs = write(span)
        Js.Obj(("__type__", "DisplaySpan"), ("__data__", spanJs))
    }
  }

  private [this] def readEventSet(js: Js): DisplayEventSet = {
    val events = js("events").arr.map(e=>readEvent(e)).toArray
    val lengthPips = js("lengthPips").num.toLong
    new DisplayEventSet(events, lengthPips)
  }

  private [this] def writeEventSet(displayEventSet: DisplayEventSet): Js = {
    Js.Obj(
      ("lengthPips", displayEventSet.lengthPips.toDouble),
      ("events", Js.Arr(displayEventSet.events.map(e=>writeEvent(e)):_*))
    )
  }

  private [this] def readGrandStaffMeasure(js: Js): DisplayGrandStaffMeasure = {
    val timeSig = read[TimeSig](js("timeSig").str)
    val bpm = js("bpm").num.toFloat
    val key = read[Key](js("key").str)
    val events = js("events").arr.map(e=>readEventSet(e)).toArray
    new DisplayGrandStaffMeasure(timeSig, bpm, key, events)
  }

  private [this] def writeGrandStaffMeasure(grandStaffMeasure: DisplayGrandStaffMeasure): Js = {
    Js.Obj(
      ("timeSig", write(grandStaffMeasure.timeSig)),
      ("bpm", grandStaffMeasure.bpm),
      ("key", write(grandStaffMeasure.key)),
      ("events", Js.Arr(grandStaffMeasure.events.map(e=>writeEventSet(e)):_*))
    )
  }

  private [this] def readStaff(js: Js): DisplayStaff = {
    val typeKey = js("__type__").str
    typeKey match {
      case "DisplayGrandStaff" =>
        val measuresJs = js("measures").arr
        val measures = measuresJs.map(m=>readGrandStaffMeasure(m)).toArray
        new DisplayGrandStaff(measures)
      case "DisplayStaffSimple" => ???
    }
  }

  private [this] def writeStaff(displayStaff: DisplayStaff): Js = {
    displayStaff match {
      case displayGrandStaff: DisplayGrandStaff => Js.Obj(
        ("__type__", "DisplayGrandStaff"),
        ("measures", Js.Arr(displayGrandStaff.measures.map(m=>writeGrandStaffMeasure(m)):_*))
      )
      case displayStaffSimple: DisplayStaffSimple => ???
    }
  }

  private [this] def readSystem(js: Js): DisplaySystem = {
    val staffsJs = js("staffs").arr
    val staffs = staffsJs.map(s=>readStaff(s)).toArray
    new DisplaySystem(staffs)
  }

  private [this] def writeSystem(system: DisplaySystem): Js = {
    val staffs = system.staffs.map(s=>writeStaff(s))
    Js.Obj(
      ("staffs", Js.Arr(staffs:_*))
    )
  }

  private [this] def readPiece(js: Js): DisplayPiece  = {
    new DisplayPiece(readSystem(js("system")))
  }

  private [this] def writePiece(piece: DisplayPiece): Js = {
    Js.Obj(
      ("system", writeSystem(piece.system))
    )
  }

  private [this] def readDocument(js: Js, document: Document): Unit = {
    readDisplayParams(js("displayParams"), document.displayParams)
    document.displayPiece = readPiece(js("displayPiece"))
    document.cursor = read[Cursor](js("cursor").str)
    document.selection = read[Selection](js("selection").str)
  }

  private [this] def writeDocument(document: Document): Js = {
    Js.Obj(
      ("displayParams", writeDisplayParams(document.displayParams)),
      ("displayPiece", writePiece(document.displayPiece)),
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
