package com.matt.tramfinder.model

import com.lucidchart.open.xtract.XmlReader
import com.matt.tramfinder.model.ModelXmlReaders.{dataFileReader, dayReader, lineReader, stopReader, timeBoardReader, timeReader, varianceReader}
import munit.CatsEffectSuite

import scala.xml.XML

class ModelXmlReadersTest extends CatsEffectSuite {

  test("time is decoded") {
    val xmlTime = """<godz h="4">
                    |<min m="45">
                    |</min>
                    |</godz>""".stripMargin

    val time: Time = XmlReader.of[Time].read(XML.loadString(xmlTime)).getOrElse(null)

    assertEquals(time, Time(4, 45))
  }

  test("day is decoded") {
    val xmlDay =
      """<dzien nazwa="w dni robocze">
        |      <godz h="4">
        |       <min m="45">
        |       </min>
        |      </godz>
        |      <godz h="5">
        |       <min m="15">
        |       </min>
        |       </godz>
        |       </dzien>""".stripMargin

    val day: Day = XmlReader.of[Day].read(XML.loadString(xmlDay)).getOrElse(null)

    assertEquals(day, Day(DayType.WorkingDay, Seq(Time(4, 45), Time(5, 15))))
  }

  test("Time board is decoded") {
    val xmlBoard = loadXml("/tabliczka.xml")
    val board: TimeBoard = XmlReader.of[TimeBoard].read(xmlBoard).getOrElse(null)

    assertEquals(board.id, 1)
    assertEquals(board.days.head.name, DayType.WorkingDay)
    assertEquals(board.days.head.times.head, Time(4, 45))
  }

  test("Stop is decoded") {
    val xmlStop = loadXml("/przystanek.xml")
    val stop: Stop = XmlReader.of[Stop].read(xmlStop).getOrElse(null)

    assertEquals(stop.id, 16514)
    assertEquals(stop.times.length, 2)
    assertEquals(stop.board.get.id, 64)
    assertEquals(stop.street, "Sowia")
  }

  test("Variance is decoded") {
    val xmlVariance = loadXml("/wariant.xml")
    val variance: Variance = XmlReader.of[Variance].read(xmlVariance).getOrElse(null)

    assertEquals(variance.id, 1)
    assertEquals(variance.stops.length, 34)
  }

  test("Line is decoded") {
    val xmlLine = loadXml("/linia.xml")
    val line: Line = XmlReader.of[Line].read(xmlLine).getOrElse(null)

    assertEquals(line.name, "A")
    assertEquals(line.`type`, "Pospieszna autobusowa")
    assertEquals(line.variances.length, 7)
  }

  test("Data file is decoded") {
    val xmlFile = loadXml("/000a.xml")
    val file = XmlReader.of[DataFile].read(xmlFile)
    file.errors.foreach(println)

    assertEquals(file.getOrElse(null).lines.length, 1)
  }
  private def loadXml(name: String) = XML.load(getClass.getResourceAsStream(name))

}
