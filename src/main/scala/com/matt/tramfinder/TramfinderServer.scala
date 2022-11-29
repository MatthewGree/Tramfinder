package com.matt.tramfinder

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import com.matt.tramfinder.repository.Repository
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import java.io.File
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.XML

object TramfinderServer {
  private def repoPrintSize(repository: Repository) = println(repository.getAllLines.size)

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      repo: Repository = getClass.getResource("/data").getPath
        .pipe(new File(_))
        .tap(file => println(file.getAbsolutePath))
        .listFiles
        .tap(_.foreach(println))
        .toList
        .map(XML.loadFile)
        .pipe(Repository.loadFromXml)
        .fold(_ => Repository.empty, identity)

      helloWorldAlg = HelloWorld.impl[F].tap(_ => repoPrintSize(repo))
      jokeAlg = Jokes.impl[F](client)



      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        TramfinderRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        TramfinderRoutes.jokeRoutes[F](jokeAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
