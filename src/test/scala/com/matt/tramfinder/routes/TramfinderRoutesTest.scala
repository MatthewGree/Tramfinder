package com.matt.tramfinder.routes

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import com.matt.tramfinder.graph.TramStop
import com.matt.tramfinder.graph.routefinder.{Duration, Route}
import com.matt.tramfinder.routes.Http4sOps.check
import com.matt.tramfinder.routes.model.RouteRequest
import com.matt.tramfinder.service.TramfinderService
import io.circe.syntax.EncoderOps
import munit.FunSuite
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonDecoder
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Method, Request, Status}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}

import java.time.Instant

class TramfinderRoutesTest extends FunSuite {
  private val service = mock[TramfinderService](classOf[TramfinderService])
  private val stops = List(TramStop(1, "stop1"))
  private val exampleRoute = Route(List.empty, Duration(20, 20))

  private val routes = new TramfinderRoutes[IO](service).routes

  override def beforeAll(): Unit = {
    when(service.getAllStops).thenReturn(stops)
    when(service.findRoute(any[Int], any[Int], any[Instant])).thenReturn(exampleRoute.asRight)
  }

  test("all nodes route should respond") {
    val result = routes.run(getNodesRequest).value.unsafeRunSync().get
    check(result, Status.Ok, stops.asJson.some)
  }

  test("route node should respond") {
    val result = routes.run(getRouteRequest).value.unsafeRunSync().get
    check(result, Status.Ok, exampleRoute.asJson.some)
  }

  private def getNodesRequest: Request[IO] =
    Request(method = Method.GET, uri = uri"/stops")

  private def getRouteRequest: Request[IO] =
    Request[IO](method = Method.GET, uri = uri"/route").withEntity(RouteRequest(1, 2, Instant.now))


}
