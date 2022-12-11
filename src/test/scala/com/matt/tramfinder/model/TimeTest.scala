package com.matt.tramfinder.model

import com.matt.tramfinder.graph.routefinder.Duration
import munit.FunSuite

class TimeTest extends FunSuite {

  test("should add 5 minutes to 8:10") {
    val time = Time(8, 10) + 5
    assertEquals(time, Time(8, 15))
  }

  test("should add 70 minutes to 8:10") {
    val time = Time(8, 10) + 70
    assertEquals(time, Time(9, 20))
  }

  test("should add 30 minutes to 23:55") {
    val time = Time(23, 55) + 30
    assertEquals(time, Time(0, 25))
  }

  test("should calculate difference between 13:20 and 12:00") {
    val diff = Time(13, 20) - Time(12, 0)
    assertEquals(diff, Duration(1, 20))
  }

  test("should calculate difference between 12:00 and 13:20") {
    val diff = Time(12, 0) - Time(13, 20)
    assertEquals(diff, Duration(22, 40))
  }

  test("should calculate difference between 23:20 and 0:20") {
    val diff = Time(0, 20) - Time(23, 20)
    assertEquals(diff, Duration(1, 0))
  }

  test("should calculate difference between 23:40 and 0:20") {
    val diff = Time(0, 20) - Time(23, 40)
    assertEquals(diff, Duration(0, 40))
  }
  test("should calculate difference between 23:40 and 23:39") {
    val diff = Time(23, 39) - Time(23, 40)
    assertEquals(diff, Duration(23, 59))
  }
}
