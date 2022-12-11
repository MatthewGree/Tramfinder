package com.matt.tramfinder.graph.routefinder

import cats.implicits.catsSyntaxEitherId
import com.matt.tramfinder.graph._
import com.matt.tramfinder.model.DayType.DayType
import munit.FunSuite

import java.time.Instant
import scala.collection.immutable.HashMap

class DijkstraRouteFinderTest extends FunSuite {

  private val finder = new DijkstraRouteFinder

  // 11.12.22 8:00
  private val time = Instant.ofEpochSecond(1670745600)

  test("it should return node not found error for empty graph") {
    val graph = Graph(HashMap.empty, HashMap.empty)
    val result = finder.findBestRoute(graph, 1, 10, time)
    assertEquals(result, StopNotFound(1).asLeft)
  }

  test("it should return route not found error on not connected start and end") {
    val graph = createGraph(4)(
      (1, 2, edgeInfo(5, 10)),
      (2, 3, edgeInfo(5, 30))
    )

    val result = finder.findBestRoute(graph, 1, 4, time)
    assertEquals(result, RouteNotFound(1, 4).asLeft)
  }

  test("it should find only path") {
    val graph = createGraph(4)(
      (1, 2, edgeInfo(5, 10)),
      (2, 3, edgeInfo(5, 30)),
      (3, 4, edgeInfo(5, 30))
    )

    val result = finder.findBestRoute(graph, 1, 4, time)
    assert(result.isRight)
    val connections = result.getOrElse(List.empty)
    assertEquals(connections.length, 3)
    asssertConnections(connections)(
      (1, 2),
      (2, 3),
      (3,4)
    )
  }

  private def asssertConnections(connections: List[Connection])(assertedConnections: (Int, Int)*): Unit =
    assertedConnections.foreach { case (from, to) =>
      assert(connections.exists(conn => conn.from.id == from && conn.to.id == to))
    }

  private def edgeInfo(timeCost: Int, timestampOffset: Int, dayType: DayType = time.getDayType, lineId: LineId = LineId("A", 2)) =
    EdgeInfo(timeCost, time.getTime + timestampOffset, dayType, lineId)

  private def createGraph(size: Int)(edges: (Int, Int, EdgeInfo)*) = {
    val nodeMap = (1 to size).map(id => id -> Node(id, s"stop$id")).toMap
    val edgeMap =
      edges.map { case (from, to, info) => (from, Edge(nodeMap(to), info)) }
        .groupBy(_._1)
        .map { case (key, value) => (key, value.map(_._2)) }

    Graph(nodeMap, edgeMap)
  }

}
