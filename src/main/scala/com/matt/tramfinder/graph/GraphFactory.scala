package com.matt.tramfinder.graph

import com.matt.tramfinder.logging.Logging
import com.matt.tramfinder.model.{Destination, Line, Stop}

import scala.collection.immutable.HashMap
import scala.util.chaining.scalaUtilChainingOps

object GraphFactory extends Logging {

  private def createEdgesFromStop(nodeMap: HashMap[String, TramStop], stop: Stop, lineId: LineId): Iterable[(TramStop, Edge)] = {
    logger.info(s"Creating edges from stop ${(stop.id, stop.name)}")
    for {
      board <- stop.board.toList
      day <- board.days
      dayTime <- day.toDayTimes
      List(start, target) <- stop.times.sliding(2)
    } yield (nodeMap(start.name), Edge(nodeMap(target.name), EdgeInfo(target.time - start.time, dayTime.time + start.time, dayTime.day, lineId)))
  }

  private def createEdges(nodeMap: HashMap[String, TramStop], lines: Iterable[Line]): Map[Int, Iterable[Edge]] = {
    (for {
      line <- lines
      variance <- line.variances
      stop <- variance.stops
      edge <- createEdgesFromStop(nodeMap, stop, LineId(line.name, variance.id))
    } yield edge)
      .groupBy { case (stop, _) => stop.id }
      .view.mapValues(_.map { case (_, edge) => edge }).toMap
  }


  def fromLines(lines: Iterable[Line]): Graph = {
    val nodeMap = getStops(lines)
      .foldLeft(HashMap.empty[String, TramStop])((map, node) =>
        map + (node.name -> node)
      )
      .pipe(map => map ++ getMissingStopsFromDestinations(lines, map).map(node => node.name -> node))

    val edges = createEdges(nodeMap, lines)

    Graph(nodeMap.map{case (_, edge) => (edge.id, edge)}, edges)
  }

  private def getStops(lines: Iterable[Line]): Iterable[TramStop] =
    for {
      line <- lines
      variance <- line.variances
      stop <- variance.stops
    } yield TramStop(stop.id, stop.name)


  private def getMissingStopsFromDestinations(lines: Iterable[Line], map: Map[String, _]): Iterable[TramStop] =
    (for {
      line <- lines
      variance <- line.variances
      stop <- variance.stops
      dest <- stop.times
    } yield if (map.contains(dest.name)) None else Some(TramStop(dest.id, dest.name))).flatten

}
