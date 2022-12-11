package com.matt.tramfinder.logging

import com.typesafe.scalalogging.Logger

trait Logging {
  protected val logger: Logger = Logger(getClass.getName)
}
