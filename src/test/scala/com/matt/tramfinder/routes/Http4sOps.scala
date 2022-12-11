package com.matt.tramfinder.routes

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.{EntityDecoder, Response, Status}

private[routes] object Http4sOps {
  def check[A](actual: Response[IO],
               expectedStatus: Status,
               expectedBody: Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): Boolean = {
    val statusCheck = actual.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actual.body.compile.toVector.unsafeRunSync().isEmpty)( // Verify Response's body is empty.
      expected => actual.as[A].unsafeRunSync() == expected
    )
    statusCheck && bodyCheck
  }
}
