package com.matt.tramfinder.graph

import com.matt.tramfinder.model.DayType.{DayType, Saturday, Sunday, WorkingDay}
import com.matt.tramfinder.model.Time

import java.time.DayOfWeek._
import java.time.{Instant, ZoneOffset}

package object routefinder {
  private[routefinder] implicit class RichGraph(graph: Graph) {
    def getNodeEither(nodeId: Int): Either[StopNotFound, Node] =
      graph.getNode(nodeId).toRight(StopNotFound(nodeId))
  }

  private[routefinder] implicit class RichTime(time: Time) {
    def isInRange(hours: Int, time: Time): Boolean =
      (1 to hours).map(delta => this.time ++ delta).foldLeft(false)((inRange, deltaTime) =>
        inRange || (time.hour < deltaTime.hour || (time.hour == deltaTime.hour && time.minutes <= deltaTime.minutes))
      )
  }

  private[routefinder] implicit class RichInstant(instant: Instant) {
    private val zoned = instant.atZone(ZoneOffset.ofHours(0))

    def getDayType: DayType = zoned.getDayOfWeek match {
      case SATURDAY => Saturday
      case SUNDAY => Sunday
      case _ => WorkingDay
    }

    def getTime: Time = Time(zoned.getHour, zoned.getMinute)
  }
}
