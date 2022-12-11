package com.matt.tramfinder.graph.routefinder

import cats.Monoid
import cats.implicits.catsSyntaxEitherId
import com.matt.tramfinder.graph._
import com.matt.tramfinder.model.DayType.DayType
import com.matt.tramfinder.model.Time
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

  test("it should return route not found if route not possible due to times") {
    val graph = createGraph(4)(
      (1, 2, edgeInfo(5, 10)),
      (2, 3, edgeInfo(5, 30)),
      (3, 4, edgeInfo(5, 30))
    )

    val result = finder.findBestRoute(graph, 1, 4, time)
    assertEquals(result, RouteNotFound(1, 4).asLeft)
  }

  test("it should find only path") {
    val graph = createGraph(4)(
      (1, 2, edgeInfo(5, 10)),
      (2, 3, edgeInfo(5, 30)),
      (3, 4, edgeInfo(5, 40))
    )

    val result = finder.findBestRoute(graph, 1, 4, time)
    assert(result.isRight)
    val route = result.getOrElse(Route(List.empty, Monoid.empty[Duration]))
    assertConnections(route, Duration(0, 45))(
      ((1, Time(8, 10)), (2, Time(8, 15))),
      ((2, Time(8, 30)), (3, Time(8, 35))),
      ((3, Time(8, 40)), (4, Time(8, 45)))
    )
  }

  test("should find shortest path") {
    val graph = createGraph(4)(
      (1, 2, edgeInfo(5, 10)),
      (2, 3, edgeInfo(5, 30)),
      (3, 4, edgeInfo(5, 40)),
      (1, 2, edgeInfo(5, 0)),
      (2, 3, edgeInfo(5, 20)),
      (3, 4, edgeInfo(2, 30))
    )

    val result = finder.findBestRoute(graph, 1, 4, time)
    assert(result.isRight)
    val route = result.getOrElse(Route(List.empty, Monoid.empty[Duration]))
    assertConnections(route, Duration(0, 32))(
      ((1, Time(8, 0)), (2, Time(8, 5))),
      ((2, Time(8, 20)), (3, Time(8, 25))),
      ((3, Time(8, 30)), (4, Time(8, 32)))
    )
  }

  private def assertConnections(route: Route, time: Duration)(assertedConnections: ((Int, Time), (Int, Time))*): Unit = {
    assertEquals(route.duration, time)
    assertedConnections.foreach { case ((from, startTime), (to, endTime)) =>
      assert(route.connections.exists(conn => conn.from.id == from && conn.to.id == to && conn.startingTime == startTime && conn.endingTime == endTime))
    }
  }

  private def edgeInfo(timeCost: Int, timestampOffset: Int, dayType: DayType = time.getDayType, lineId: LineId = LineId("A", 2)) =
    EdgeInfo(timeCost, time.getTime + timestampOffset, dayType, lineId)

  private def createGraph(size: Int)(edges: (Int, Int, EdgeInfo)*) = {
    val nodeMap = (1 to size).map(id => id -> TramStop(id, s"stop$id")).toMap
    val edgeMap =
      edges.map { case (from, to, info) => (from, Edge(nodeMap(to), info)) }
        .groupBy(_._1)
        .map { case (key, value) => (key, value.map(_._2)) }

    Graph(nodeMap, edgeMap)
  }

}
