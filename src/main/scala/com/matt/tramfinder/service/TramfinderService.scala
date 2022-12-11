package com.matt.tramfinder.service

import com.matt.tramfinder.graph.routefinder.{Route, RouteError, RouteFinder}
import com.matt.tramfinder.graph.{Graph, TramStop}
import com.matt.tramfinder.logging.Logging

import java.time.Instant
import scala.util.chaining.scalaUtilChainingOps

class TramfinderService(graph: Graph, routeFinder: RouteFinder) extends Logging {
  def getAllStops: List[TramStop] =
    graph.getAllStops.toList

  def findRoute(startStopId: Int, endStopId: Int, time: Instant): Either[RouteError, Route] = {
    logger.info(s"Searching for route from ${graph.getStop(startStopId)} to ${graph.getStop(endStopId)} with time: $time")
    routeFinder.findBestRoute(graph, startStopId, endStopId, time)
      .tap(result => logger.info(s"Route from ${graph.getStop(startStopId)} to ${graph.getStop(endStopId)}: $result"))
  }
}
