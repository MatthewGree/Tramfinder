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
import fs2.io.net.tls.TLSContext
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{CORS, CORSPolicy, Logger}

import scala.util.chaining.scalaUtilChainingOps

class TramfinderServer(repository: Repository, ip: String, port: String) {
  def stream[F[_] : Async]: Stream[F, Nothing] = {
    val graph = GraphFactory.fromLines(repository.getAllLines)
    val service = new TramfinderService(graph, new DijkstraRouteFinder)
    val tramRoutes = new TramfinderRoutes[F](service)
    val tlsContext = TLSContext.Builder.forAsync[F].fromKeyStoreResource("certs/store.keystore", "cert1cert1".toCharArray, "cert1".toCharArray)
    val httpApp =
      tramRoutes
        .routes
        .orNotFound
        .pipe(Logger.httpApp(true, true))
        .pipe(CORS.policy.httpApp)

    for {
      context <- Stream.resource(Resource.eval(tlsContext))
      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(Ipv4Address.fromString(ip).get)
          .withPort(Port.fromString(port).get)
          .withHttpApp(httpApp)
          .withTLS(context)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
