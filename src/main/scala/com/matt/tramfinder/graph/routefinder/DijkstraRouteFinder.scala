package com.matt.tramfinder.graph.routefinder

import cats.implicits.{catsSyntaxOptionId, catsSyntaxOrder, catsSyntaxPartialOrder, catsSyntaxSemigroup}
import com.matt.tramfinder.graph.{Graph, LineId, TramStop}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

class DijkstraRouteFinder extends RouteFinder {

  private case class StopReached(stop: TramStop, duration: Duration, lineId: LineId)

  private implicit val stopOrdering: Ordering[StopReached] =
    (x: StopReached, y: StopReached) => (y.duration.hours, y.duration.minutes).compare((x.duration.hours, x.duration.minutes))


  override def findBestRoute(graph: Graph, start: Int, end: Int, time: Instant): Either[RouteError, Route] = {
    for {
      startStop <- graph.getStopEither(start)
      endStop <- graph.getStopEither(end)
      route <- calculateShortestRoute(startStop, endStop, graph, time)
    } yield route
  }

  private def calculateShortestRoute(startStop: TramStop, endStop: TramStop, graph: Graph, time: Instant): Either[RouteNotFound, Route] = {
    val durations = new mutable.HashMap[TramStop, Duration]()
    val prev = new mutable.HashMap[TramStop, Connection]()

    val queue = new mutable.PriorityQueue[StopReached]()
    graph.getAllStops.foreach(stop =>
      if (stop.id != startStop.id) {
        durations.addOne(stop -> Duration(Int.MaxValue, Int.MaxValue))
      }
    )
    queue.addOne(StopReached(startStop, Duration(0, 0), LineId("", 0)))
    durations.addOne(startStop -> Duration(0, 0))

    var targetNotFound = true
    whileContinuable(queue.nonEmpty && targetNotFound) {
      val stopReached = queue.dequeue()
      if (stopReached.stop.id == endStop.id) {
        targetNotFound = false
        break()
      }
      if (stopReached.duration != durations(stopReached.stop)) {
        break()
      }
      val stopInstant = time.plus(stopReached.duration)
      graph.getStopEdges(stopReached.stop)
        .filter(edge => {
          val timeWhenReady = stopInstant.plus((if (stopReached.lineId != edge.info.lineId) 5 else 0), ChronoUnit.MINUTES)
          timeWhenReady.getTime.isInRange(3, edge.info.time) &&
            timeWhenReady.getDayType == edge.info.dayType
        }).foreach { edge =>
        val newDuration =
          stopReached.duration combine
            Duration(0, edge.info.timeCost) combine
            (edge.info.time - stopInstant.getTime)
        if (newDuration < durations(edge.stop)) {
          durations.update(edge.stop, newDuration)
          prev.update(edge.stop, Connection(stopReached.stop, edge.stop, edge.info.time, edge.info.time + edge.info.timeCost, edge.info.lineId))
          queue.addOne(StopReached(edge.stop, newDuration, edge.info.lineId))
        }
      }
    }

    extractConnections(prev, startStop, endStop)
      .flatMap(connections => durations.get(endStop).map(Route(connections, _))) match {
      case Some(route) => Right(route)
      case None => Left(RouteNotFound(startStop.id, endStop.id))
    }
  }

  private def extractConnections(prev: mutable.Map[TramStop, Connection], startStop: TramStop, endStop: TramStop): Option[List[Connection]] =
    prev.get(endStop).flatMap { connection =>
      if (connection.from == startStop) {
        List(connection).some
      } else {
        extractConnections(prev, startStop, connection.from).map(_.appended(connection))
      }
    }

  private def whileContinuable(condition: => Boolean)(statement: => Unit): Unit =
    while (condition) {
      breakable(statement)
    }
}
