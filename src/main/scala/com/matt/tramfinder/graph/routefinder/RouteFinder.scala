package com.matt.tramfinder.graph.routefinder

import com.matt.tramfinder.graph.{Graph, LineId, Node}
import com.matt.tramfinder.model.Time

import java.time.Instant

trait RouteFinder {
  def findBestRoute(graph: Graph, start: Int, end: Int, time: Instant): Either[RouteError, Route]
}

case class Duration(hour: Int, minutes: Int)

case class Connection(from: Node, to: Node, startingTime: Time, endingTime: Time, line: LineId)

case class Route(connections: List[Connection], duration: Duration)

sealed trait RouteError {
  def errorMessage: String
}

case class StopNotFound(stopId: Int) extends RouteError {
  override def errorMessage: String = s"Stop $stopId was not found"
}

case class RouteNotFound(startStopId: Int, endStopId: Int) extends RouteError {
  override def errorMessage: String = s"Route between stops $startStopId and $endStopId was not found"
}
