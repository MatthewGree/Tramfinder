package com.matt.tramfinder

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import com.matt.tramfinder.graph.GraphFactory
import com.matt.tramfinder.graph.routefinder.DijkstraRouteFinder
import com.matt.tramfinder.repository.Repository
import com.matt.tramfinder.routes.TramfinderRoutes
import com.matt.tramfinder.service.TramfinderService
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import scala.util.chaining.scalaUtilChainingOps

class TramfinderServer(repository: Repository, ip: String, port: String) {
  def stream[F[_] : Async]: Stream[F, Nothing] = {

    val graph = GraphFactory.fromLines(repository.getAllLines)
    val service = new TramfinderService(graph, new DijkstraRouteFinder)
    val tramRoutes = new TramfinderRoutes[F](service)
    val httpApp =
      tramRoutes
        .routes
        .orNotFound
        .pipe(Logger.httpApp(true, true))

    for {
      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"$ip")
          .withPort(port"$port")
          .withHttpApp(httpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
