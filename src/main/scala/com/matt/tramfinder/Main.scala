package com.matt.tramfinder

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    TramfinderServer.stream[IO].compile.drain.as(ExitCode.Success)
}
