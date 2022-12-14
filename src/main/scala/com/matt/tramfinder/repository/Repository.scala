package com.matt.tramfinder.repository

import cats.implicits.toTraverseOps
import com.lucidchart.open.xtract.{ParseError, XmlReader}
import com.matt.tramfinder.model.ModelXmlReader.dataFileReader
import com.matt.tramfinder.model.{DataFile, Line}

import java.io.File
import scala.collection.immutable.HashMap
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, XML}

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
      .sequence
      .map(_.flatMap(_.lines))
      .map(_.foldLeft(HashMap.empty[String, Line])((db, line) => db + (line.name -> line)))
      .map(new Repository(_))
      .fold[Either[Seq[ParseError], Repository]](Left(_))(Right(_))

  def loadFromPath(pathName: String): Either[Seq[ParseError], Repository] = {
    new File(pathName)
      .listFiles
      .toList
      .map(XML.loadFile)
      .pipe(Repository.loadFromXml)
  }


  def empty: Repository =
    new Repository(HashMap.empty)
}
