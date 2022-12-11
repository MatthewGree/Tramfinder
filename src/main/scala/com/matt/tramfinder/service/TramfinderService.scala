package com.matt.tramfinder.service

import com.matt.tramfinder.graph.routefinder.{Route, RouteError, RouteFinder}
import com.matt.tramfinder.graph.{Graph, TramStop}

import java.time.Instant

class TramfinderService(graph: Graph, routeFinder: RouteFinder) {
  def getAllStops: List[TramStop] =
    graph.getAllStops.toList

  def findRoute(startStopId: Int, endStopId: Int, time: Instant): Either[RouteError, Route] =
    routeFinder.findBestRoute(graph, startStopId, endStopId, time)
}
