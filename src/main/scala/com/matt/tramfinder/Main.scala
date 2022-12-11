package com.matt.tramfinder

import cats.effect.{ExitCode, IO, IOApp}
import com.lucidchart.open.xtract.ParseError
import com.matt.tramfinder.logging.Logging
import com.matt.tramfinder.repository.Repository

import scala.util.chaining.scalaUtilChainingOps

object Main extends IOApp with Logging {
  def run(args: List[String]): IO[ExitCode] = {
    Repository
      .loadFromPath(args.head)
      .fold(cancelWithErrors, runServer)
  }

  private def runServer(repository: Repository): IO[ExitCode] =
    new TramfinderServer(repository).stream[IO].compile.drain.as(ExitCode.Success)

  private def cancelWithErrors(errors: Seq[ParseError]): IO[ExitCode] =
    IO(ExitCode.Error).tap(_ => logger.error(errors.toString))

}
