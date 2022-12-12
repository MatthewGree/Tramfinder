package com.matt.tramfinder.routes

import cats.effect.kernel.Concurrent
import cats.implicits.toSemigroupKOps
import com.matt.tramfinder.service.TramfinderService
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant

class TramfinderRoutes[F[_] : Concurrent](service: TramfinderService) extends Http4sDsl[F] {
  private object StartStopQueryParamMatcher extends QueryParamDecoderMatcher[Int]("start")

  private object TargetStopQueryParamMatcher extends QueryParamDecoderMatcher[Int]("target")

  private implicit val instantDecoder: QueryParamDecoder[Instant] = QueryParamDecoder[String]
    .map(URLDecoder.decode(_, StandardCharsets.UTF_8))
    .map(_.asJson)
    .emap(_.as[Instant].fold(error => Left(ParseFailure("", error.message)), Right.apply))

  private object TimeQueryParamMatcher extends QueryParamDecoderMatcher[Instant]("time")

  def routes: HttpRoutes[F] =
    getAllNodes <+> findRoute


  private[routes] def getAllNodes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "stops" =>
      Ok(service.getAllStops)
  }

  private[routes] def findRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "route"
      :? StartStopQueryParamMatcher(from)
      +& TargetStopQueryParamMatcher(to)
      +& TimeQueryParamMatcher(time) =>
      service.findRoute(from, to, time)
        .fold(error => NotFound(error.errorMessage), route => Ok(route))
  }
}