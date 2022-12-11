package com.matt.tramfinder.routes.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

case class RouteRequest(from: Int, to: Int, time: Instant)

object RouteRequest {
  implicit val routeRequestCodec: Codec[RouteRequest] = deriveCodec
}
