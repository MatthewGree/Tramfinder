package com.matt.tramfinder.model

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


case class Time(hour: Int, minutes: Int)

case class Day(name: DayType, times: Seq[Time])

case class TimeBoard(id: Int, mc: Int, days: Seq[Day])

case class Destination(number: Int, id: Int, name: String, time: Int)

case class Stop(id: Int, name: String, street: String, properties: String, times: Seq[Destination], board: Option[TimeBoard])

case class Variance(id: Int, name: String, stops: Seq[Stop])

case class Line(name: String, `type`: String, validFrom: String, validTo: String, variances: Seq[Variance])

case class DataFile(lines: Seq[Line])
