package com.matt.tramfinder.graph

import com.matt.tramfinder.model.DayType.DayType
import com.matt.tramfinder.model.Time

case class TramStop(id: Int, name: String)


case class EdgeInfo(timeCost: Int, time: Time, dayType: DayType, lineId: LineId)

case class LineId(lineId: String, varianceId: Int)

case class Edge(stop: TramStop, info: EdgeInfo)

case class Graph private[graph](private val stops: Map[Int, TramStop], private val edges: Map[Int, Iterable[Edge]]) {
  def getStop(stopId: Int): Option[TramStop] = stops.get(stopId)

  def getAllStops: Iterable[TramStop] = stops.values

  def getStopEdges(stop: TramStop): Iterable[Edge] = edges.getOrElse(stop.id, Seq.empty)
}
