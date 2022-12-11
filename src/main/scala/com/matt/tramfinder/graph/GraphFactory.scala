package com.matt.tramfinder.graph

import com.matt.tramfinder.model.{Line, Stop}

import scala.collection.immutable.HashMap
import scala.util.chaining.scalaUtilChainingOps

object GraphFactory {

  private def createEdgesFromStop(nodeMap: HashMap[Int, TramStop], stop: Stop, lineId: LineId): Seq[Edge] =
    stop.board.map(_.days.map(_.toDayTimes).zip(stop.times).flatMap { case (dayTimes, dest) =>
      dayTimes.map(dayTime => Edge(nodeMap(dest.id), EdgeInfo(dest.time, dayTime.time + dest.time, dayTime.day, lineId)))
    }).getOrElse(Seq.empty)

  private def createEdges(nodeMap: HashMap[Int, TramStop], lines: Iterable[Line]): Map[Int, Seq[Edge]] =
    lines.flatMap(line =>
      line.variances.flatMap(variance =>
        variance.stops.map(
          stop => stop.id -> createEdgesFromStop(nodeMap, stop, LineId(line.name, variance.id))
        )
      )
    ).toMap


  def fromLines(lines: Iterable[Line]): Graph = {
    val nodeMap = getStops(lines)
      .foldLeft(HashMap.empty[Int, TramStop])((map, node) =>
        map + (node.id -> node)
      )
      .pipe(map => map ++ getMissingStopsFromDestinations(lines, map).map(node => node.id -> node))

    val edges = createEdges(nodeMap, lines)

    Graph(nodeMap, edges)
  }

  private def getStops(lines: Iterable[Line]): Iterable[TramStop] =
    lines
      .flatMap(
        _.variances.flatMap(
          _.stops.map(stop => TramStop(stop.id, stop.name))
        )
      )

  private def getMissingStopsFromDestinations(lines: Iterable[Line], map: Map[Int, _]): Iterable[TramStop] =
    lines.flatMap(
      _.variances.flatMap(
        _.stops.flatMap(
          _.times.map(
            dest => if (map.contains(dest.id)) {
              None
            } else {
              Some(TramStop(dest.id, dest.name))
            }
          )
        )
      )
    ).flatten
}
