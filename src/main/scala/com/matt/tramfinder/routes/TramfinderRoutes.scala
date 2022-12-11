package com.matt.tramfinder.routes

import cats.effect.kernel.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import com.matt.tramfinder.routes.model.RouteRequest
import com.matt.tramfinder.service.TramfinderService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class TramfinderRoutes[F[_] : Concurrent](service: TramfinderService) extends Http4sDsl[F] {
  private implicit val routesRequestEntityDecoder: EntityDecoder[F, RouteRequest] = jsonOf[F, RouteRequest]


  def routes: HttpRoutes[F] =
    getAllNodes <+> findRoute


  private[routes] def getAllNodes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "stops" =>
      Ok(service.getAllStops)
  }

  private[routes] def findRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@GET -> Root / "route" =>
      for {
        request <- req.as[RouteRequest]
        route <- service.findRoute(request.from, request.to, request.time).fold(error => NotFound(error.errorMessage), route => Ok(route))
      } yield route
  }
}