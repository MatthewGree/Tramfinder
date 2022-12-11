package com.matt.tramfinder

import com.matt.tramfinder.graph.routefinder.{Connection, Duration, Route}
import com.matt.tramfinder.graph.{LineId, TramStop}
import com.matt.tramfinder.model.Time
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

package object routes {

  implicit val tramStopCodec: Codec[TramStop] = deriveCodec
  implicit val timeCodec: Codec[Time] = deriveCodec
  implicit val durationCodec: Codec[Duration] = deriveCodec
  implicit val connectionCoded: Codec[Connection] = deriveCodec
  implicit val lineIdCodec: Codec[LineId] = deriveCodec
  implicit val routeCodec: Codec[Route] = deriveCodec

}
