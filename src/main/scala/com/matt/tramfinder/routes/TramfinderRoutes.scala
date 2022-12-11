package com.matt.tramfinder.routes

import cats.effect.Sync
import cats.implicits._
import com.matt.tramfinder.service.TramfinderService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, QueryParamCodec}

import java.time.Instant
import java.time.format.DateTimeFormatter

class TramfinderRoutes[F[_] : Sync](service: TramfinderService) extends Http4sDsl[F] {
  private implicit val instantCodec: QueryParamCodec[Instant] = QueryParamCodec.instantQueryParamCodec(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  object FromStopQueryParamMatcher extends QueryParamDecoderMatcher[Int]("from")

  object TargetStopQueryParamMatcher extends QueryParamDecoderMatcher[Int]("target")

  object InstantQueryParamMatcher extends QueryParamDecoderMatcher[Instant]("target")

  def routes: HttpRoutes[F] =
    getAllNodes <+> findRoute


  private[routes] def getAllNodes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "stops" =>
      for {
        resp <- Ok(service.getAllStops)
      } yield resp
  }

  private[routes] def findRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "route" :? FromStopQueryParamMatcher(fromId) :? TargetStopQueryParamMatcher(targetId) :? InstantQueryParamMatcher(time) =>
      service.findRoute(fromId, targetId, time).fold(error => NotFound(error.errorMessage), route => Ok(route))
  }
}