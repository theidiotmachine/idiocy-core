package idiocy.ui.io.musicxml

import idiocy.music.key.Key
import idiocy.ui.Document
import idiocy.ui.data.TimeSig
import idiocy.ui.renderer._
import javax.xml.parsers.SAXParser

import scala.collection.mutable.ArrayBuffer
import scala.xml.Elem
import scala.xml.factory.XMLLoader


object ThisXML extends XMLLoader[Elem] {
  override def parser: SAXParser = {
    val f = javax.xml.parsers.SAXParserFactory.newInstance()
    f.setValidating(false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    f.newSAXParser()
  }
}

final case class MusicXMLPartId(id: String, instrumentName: Option[String], partName: Option[String])

object IOMusicXML {
  private [this] def divisionsToPips(durationInDivisions: Int, numDivisionsPerQuarterNote: Int): Long = {
    val numQuarterNotes = durationInDivisions.toDouble / numDivisionsPerQuarterNote.toDouble
    (DisplayEvent.PipsToABeat * numQuarterNotes).toLong
  }

  def read(dirName: String, fName: String, doc: Document): Unit = {
    val xmlElem: Elem = ThisXML.loadFile(dirName + "/" +fName)

    if(xmlElem.label != "score-partwise"){
      throw new IllegalArgumentException("Can only consume partwise scores")
    }
    val scorePartwiseElem = xmlElem

    val partListNode = scorePartwiseElem.child.find(c=>{c.label == "part-list"}).get
    val partsIds = partListNode.child.filter(
      c=>c.isInstanceOf[Elem]
    ).map(c=>{
      val oScoreInstrumentNode = c.child.find(c1=>c1.label == "score-instrument")
      val partNameNode = c.child.find(c1=>c1.label == "part-name").get

      MusicXMLPartId(c.attribute("id").get.head.text,
        oScoreInstrumentNode.map(n0=>n0.child.find(c1 => c1.label == "instrument-name").get.child.head.text),
        Option(partNameNode.text)
      )
    })

    val partNodes = partsIds.map(pid=>{
      scorePartwiseElem.child.find(c=>{c.label == "part" && c.attribute("id").get.text == pid.id}).get
    })

    val staffs = ArrayBuffer[DisplayGrandStaff]()
    var staffId = 0
    partNodes.foreach(c=>{
      val measureNodes = c.child.filter(c1=>c1.label == "measure")

      var numDivisionsPerQuarterNote = 0
      var beats = 0
      var beatType = 0
      var bpm = 120.0f

      val measures = ArrayBuffer[DisplayGrandStaffMeasure]()
      measureNodes.foreach(
        measureNode => {
          val eventSets = ArrayBuffer[DisplayEventSet]()

          measureNode.child.foreach(c1=>{
            c1.label match {
              case "attributes" =>
                numDivisionsPerQuarterNote = c1.child.find(c2=>c2.label == "divisions").get.text.toInt
                val timeNode = c1.child.find(c2=>c2.label == "time").get
                beats = timeNode.child.find(c2=>c2.label == "beats").get.text.toInt
                beatType = timeNode.child.find(c2=>c2.label == "beat-type").get.text.toInt
              case "direction" =>
                val soundNode = c1.child.find(c2=>c2.label == "sound").get
                bpm = soundNode.attribute("tempo").get.text.toFloat
              case "note" =>
                val durationNode = c1.child.find(c2=>c2.label == "duration").get
                val durationInDivisions = durationNode.text.toInt
                val durationInPips = divisionsToPips(durationInDivisions, numDivisionsPerQuarterNote)

                if(!c1.child.exists(c2 => c2.label == "rest")){
                  val pitchNode = c1.child.find(c2=>c2.label == "pitch").get
                  val step = pitchNode.child.find(c2=>c2.label == "step").get.text
                  val octave = pitchNode.child.find(c2=>c2.label == "octave").get.text.toInt

                  eventSets += new DisplayEventSet(Array[DisplayEvent](
                    new DisplayNote(durationInPips,
                      DisplayGrandStaff.pitchClassNameAndOctaveNumberToBarLine(step, octave), 0)),
                    durationInPips)
                } else {
                  eventSets += new DisplayEventSet(Array[DisplayEvent](
                    DisplayPartialRest(durationInPips,
                      0)),
                    durationInPips)
                }

              case _ =>
            }
          })
          measures += new DisplayGrandStaffMeasure(TimeSig.timeSig44, bpm, Key.CMajor, eventSets.toArray)
        }
      )

      staffs += new DisplayGrandStaff(measures.toArray)
      doc.displayParams.trackVisibility += true
      staffId += 1
    })

    doc.displayPiece = new DisplayPiece(new DisplaySystem(staffs.toArray))
  }
}
