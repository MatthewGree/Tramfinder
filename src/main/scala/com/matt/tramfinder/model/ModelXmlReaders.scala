package com.matt.tramfinder.model

import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __}

object ModelXmlReaders {

  private case class Minutes(minutes: Int)

  private implicit val minutesReader: XmlReader[Minutes] =
    for {
      minutes <- attribute[Int]("m")
    } yield Minutes(minutes)

  private[model] implicit val timeReader: XmlReader[IntermediateTime] =
    for {
      hour <- (__ \@ "h").read[Int]
      minutes <- (__ \ "min").read(seq[Minutes]).atLeast(1)
    } yield IntermediateTime(hour, minutes.map(_.minutes))

  implicit val dayReader: XmlReader[Day] =
    for {
      dayType <- (__ \@ "nazwa").read[String].map(DayType.fromString)
      times <- (__ \ "godz").read(seq[IntermediateTime]).map(_.flatMap(time => time.minutes.map(Time(time.hour, _))))
    } yield Day(dayType, times)

  implicit val timeBoardReader: XmlReader[TimeBoard] =
    for {
      id <- (__ \@ "id").read[Int]
      mc <- (__ \@ "mc").read[Int]
      days <- (__ \ "dzien").read(seq[Day])
    } yield TimeBoard(id, mc, days)

  implicit val destinationReader: XmlReader[Destination] =
    for {
      number <- (__ \@ "numer").read[Int]
      id <- (__ \@ "id").read[Int]
      name <- (__ \@ "nazwa").read[String]
      time <- (__ \@ "czas").read[Int]
    } yield Destination(number, id, name, time)

  implicit val stopReader: XmlReader[Stop] =
    for {
      id <- (__ \@ "id").read[Int]
      name <- (__ \@ "nazwa").read[String]
      street <- (__ \@ "ulica").read[String]
      props <- (__ \@ "cechy").read[String]
      times <- (__ \ "czasy" \ "przystanek").read(seq[Destination])
      board <- (__ \ "tabliczka").read[TimeBoard].optional
    } yield Stop(id, name, street, props, times, board)

  implicit val varianceReader: XmlReader[Variance] =
    for {
      id <- (__ \@ "id").read[Int]
      name <- (__ \@ "nazwa").read[String]
      stops <- (__ \ "przystanek").read(seq[Stop])
    } yield Variance(id, name, stops)

  implicit val lineReader: XmlReader[Line] =
    for {
      name <- (__ \@ "nazwa").read[String]
      kind <- (__ \@ "typ").read[String]
      validFrom <- (__ \@ "wazny_od").read[String]
      validTo <- (__ \@ "wazny_do").read[String]
      variances <- (__ \ "wariant").read(seq[Variance])
    } yield Line(name, kind, validFrom, validTo, variances)

  implicit val dataFileReader: XmlReader[DataFile] =
    for {
      lines <- (__ \ "linia").read(seq[Line])
    } yield DataFile(lines)

}
