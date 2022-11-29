package com.matt.tramfinder.repository

import cats.implicits.toTraverseOps
import com.lucidchart.open.xtract.{ParseError, XmlReader}
import com.matt.tramfinder.model.{DataFile, Line}
import com.matt.tramfinder.model.ModelXmlReaders.{dataFileReader}

import scala.collection.immutable.HashMap
import scala.xml.Elem

class Repository(private val db: Map[String, Line]) {
  def getLine(name: String): Option[Line] =
    db.get(name)

  def getAllLines: Iterable[Line] =
    db.values
}

object Repository {
  def loadFromXml(xmlElems: List[Elem]): Either[Seq[ParseError], Repository] =
    xmlElems
      .map(XmlReader.of[DataFile].read)
      .map(_.map(_.lines))
      .sequence
      .map(_.flatten)
      .map(_.foldLeft(HashMap.empty[String, Line])((db, line) => db + (line.name -> line)))
      .map(new Repository(_))
      .fold[Either[Seq[ParseError], Repository]](Left(_))(Right(_))

  def empty: Repository =
    new Repository(HashMap.empty)
}
