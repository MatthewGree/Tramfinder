package com.matt.tramfinder.routes

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import com.matt.tramfinder.graph.TramStop
import com.matt.tramfinder.graph.routefinder.{Duration, Route}
import com.matt.tramfinder.routes.Http4sOps.check
import com.matt.tramfinder.service.TramfinderService
import io.circe.syntax.EncoderOps
import munit.FunSuite
import org.http4s.circe.jsonDecoder
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Method, Request, Status, Uri}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, times, verify, when}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit

class TramfinderRoutesTest extends FunSuite {
  private val service = mock[TramfinderService](classOf[TramfinderService])
  private val stops = List(TramStop(1, "stop1"))
  private val exampleRoute = Route(List.empty, Duration(20, 20))

  private val routes = new TramfinderRoutes[IO](service).routes

  test("all nodes route should respond") {
    when(service.getAllStops).thenReturn(stops)
    val result = routes.run(getNodesRequest).value.unsafeRunSync().get
    check(result, Status.Ok, stops.asJson.some)
    verify(service, times(1)).getAllStops
  }

  test("route node should respond") {
    when(service.findRoute(any[Int], any[Int], any[Instant])).thenReturn(exampleRoute.asRight)
    val result = routes.run(getRouteRequest).value.unsafeRunSync().get
    check(result, Status.Ok, exampleRoute.asJson.some)
    verify(service, times(1)).findRoute(any[Int], any[Int], any[Instant])
  }

  private def getNodesRequest: Request[IO] =
    Request(method = Method.GET, uri = uri"/stops")

  private def getRouteRequest: Request[IO] = {
    val instant = URLEncoder.encode(Instant.now.truncatedTo(ChronoUnit.SECONDS).asJson.noSpaces.filter(_ != '\"'), StandardCharsets.UTF_8)
    Request[IO](method = Method.GET, uri = Uri.fromString(s"/route?start=1&target=2&time=$instant").toTry.get)
  }


}
