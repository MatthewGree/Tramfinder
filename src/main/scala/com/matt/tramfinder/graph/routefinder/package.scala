package com.matt.tramfinder.graph

import cats.implicits.catsSyntaxPartialOrder
import cats.{Monoid, PartialOrder}
import com.matt.tramfinder.model.DayType.{DayType, Saturday, Sunday, WorkingDay}
import com.matt.tramfinder.model.Time

import java.time.DayOfWeek._
import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneOffset}

package object routefinder {
  private[routefinder] implicit class RichGraph(graph: Graph) {
    def getStopEither(stopId: Int): Either[StopNotFound, TramStop] =
      graph.getStop(stopId).toRight(StopNotFound(stopId))
  }

  private[routefinder] implicit class RichTime(time: Time) {
    def isInRange(hours: Int, time: Time): Boolean =
      time - this.time < Duration(hours, 0)
  }

  private[routefinder] implicit class RichInstant(instant: Instant) {
    private val zoned = instant.atZone(ZoneOffset.ofHours(0))

    def getDayType: DayType = zoned.getDayOfWeek match {
      case SATURDAY => Saturday
      case SUNDAY => Sunday
      case _ => WorkingDay
    }

    def plus(duration: Duration): Instant =
      instant.plus(duration.hours.toLong, ChronoUnit.HOURS).plus(duration.minutes.toLong, ChronoUnit.MINUTES)

    def getTime: Time = Time(zoned.getHour, zoned.getMinute)
  }

  implicit val durationMonoid: Monoid[Duration] = new Monoid[Duration] {
    override def empty: Duration = Duration(0, 0)

    override def combine(x: Duration, y: Duration): Duration = {
      val (additionalHours, newMinutes) = {
        var newMinutes = x.minutes + y.minutes
        var addHours = 0
        while (newMinutes > 60) {
          newMinutes -= 60
          addHours += 1
        }
        (addHours, newMinutes)
      }
      Duration(x.hours + y.hours + additionalHours, newMinutes)
    }
  }

  implicit val durationPartialOrder: PartialOrder[Duration] = (x: Duration, y: Duration) => (x.hours, x.minutes).partialCompare((y.hours, y.minutes))
}
