package com.matt.tramfinder.repository

import munit.FunSuite

import scala.xml.XML

class RepositoryTest extends FunSuite {
  test("loads from xml correctly") {
    val xml = XML.load(getClass.getResourceAsStream("/000a.xml"))
    val repoResult = Repository.loadFromXml(List(xml))
    assert(repoResult.isRight)
    val repo = repoResult.getOrElse(null)
    val lineA = repo.getLine("A")
    assert(lineA.isDefined)
    val line = lineA.get
    assertEquals(line.`type`, "Pospieszna autobusowa")
    assertEquals(line.validFrom, "17.09.2022")

    assert(line.variances.nonEmpty)
    val variance = line.variances.head

    assertEquals(variance.id, 1)
    assertEquals(variance.name, "KRZYKI - KOSZAROWA (Szpital)")

    assertEquals(repo.getAllLines.size, 1)
  }

}
