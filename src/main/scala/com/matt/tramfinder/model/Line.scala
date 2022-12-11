package com.matt.tramfinder.model

import com.matt.tramfinder.graph.routefinder.Duration
import com.matt.tramfinder.model.DayType.DayType

object DayType extends Enumeration {
  type DayType = Value
  val WorkingDay, Saturday, Sunday = Value

  def fromString(string: String): DayType = string match {
    case "Niedziela" => Sunday
    case "Sobota" => Saturday
    case _ => WorkingDay
  }
}

private[model] case class IntermediateTime(hour: Int, minutes: Seq[Int])

case class Time(hour: Int, minutes: Int) {
  lazy val toMinutes: Int = hour * 60 + minutes


  def +(minutes: Int): Time = {
    val addedMinutes = minutes % 60
    val addedHours = minutes / 60
    var newMinutes = this.minutes + addedMinutes
    var newHours = this.hour + addedHours
    while (newMinutes >= 60) {
      newMinutes -= 60
      newHours += 1
    }
    while (newHours >= 24) {
      newHours -= 24
    }
    Time(newHours, newMinutes)
  }

  def ++(hours: Int): Time = {
    var newHours = hour + hours
    while (newHours >= 24) {
      newHours -= 24
    }
    Time(newHours, this.minutes)
  }

  def <=(time: Time): Boolean =
    this.hour < time.hour || (this.hour == time.hour && this.minutes <= time.minutes)


  def -(time: Time): Duration = {
    val minutesDiff = if (this.minutes >= time.minutes) this.minutes - time.minutes else (60 - time.minutes) + this.minutes
    val adjustedTime = time + minutesDiff
    val hourDiff =
      if (this.hour < adjustedTime.hour)
        24 - adjustedTime.hour + this.hour
      else
        this.hour - adjustedTime.hour
    Duration(hourDiff, minutesDiff)
  }

}

case class Day(name: DayType, times: Seq[Time]) {
  def toDayTimes: Seq[DayTime] = times.map(DayTime(name, _))
}

case class DayTime(day: DayType, time: Time)

case class TimeBoard(id: Int, mc: Int, days: Seq[Day])

case class Destination(number: Int, id: Int, name: String, time: Int)

case class Stop(id: Int, name: String, street: String, properties: String, times: Seq[Destination], board: Option[TimeBoard])

case class Variance(id: Int, name: String, stops: Seq[Stop])

case class Line(name: String, `type`: String, validFrom: String, validTo: String, variances: Seq[Variance])

case class DataFile(lines: Seq[Line])
