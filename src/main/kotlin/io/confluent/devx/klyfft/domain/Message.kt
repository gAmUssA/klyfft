package io.confluent.devx.klyfft.domain

import java.io.Serializable

//TODO use https://github.com/mapbox/mapbox-java 

data class Message(val driver: String?,
                   val rider: String?,
                   val status: String = "",
                   val lngLat: LngLat = LngLat(),
                   val timestamp: Long,
                   val route: Route?) : Serializable

data class LngLat(val lat: Double = 0.0, val lng: Double = 0.0) : Serializable

typealias StepsArray = Array<Steps>
typealias DoublesArray = Array<Double>
typealias LegsArray = Array<Route>

data class Route(val distance: Long,
                 val duration: Long,
                 val steps: StepsArray?,
                 val geometry: String,
                 val summary: String?,
                 var legs: LegsArray,
                 val weight: Long,
                 val weight_name: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Route

    if (distance != other.distance) return false
    if (duration != other.duration) return false
    if (steps != null) {
      if (other.steps == null) return false
      if (!steps.contentEquals(other.steps)) return false
    } else if (other.steps != null) return false
    if (geometry != other.geometry) return false
    if (summary != other.summary) return false

    return true
  }

  override fun hashCode(): Int {
    var result = distance.hashCode()
    result = 31 * result + duration.hashCode()
    result = 31 * result + (steps?.contentHashCode() ?: 0)
    result = 31 * result + geometry.hashCode()
    result = 31 * result + summary.hashCode()
    return result
  }

}

data class Steps(val distance: Long,
                 val duration: Long,
                 val way_name: String,
                 val mode: String,
                 val direction: String,
                 val heading: Long,
                 val maneuver: Instruction)

data class Instruction(val instruction: String,
                       val type: String,
                       val location: DoublesArray,
                       val bearing_after: Int,
                       val bearing_before: Int,
                       val modifier: String?) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Instruction

    if (instruction != other.instruction) return false
    if (type != other.type) return false
    if (!location.contentEquals(other.location)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = instruction.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + location.contentHashCode()
    return result
  }
}

data class Location(val type: String,
                    val coordinates: DoublesArray) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Location

    if (type != other.type) return false
    if (!coordinates.contentEquals(other.coordinates)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + coordinates.contentHashCode()
    return result
  }
}


