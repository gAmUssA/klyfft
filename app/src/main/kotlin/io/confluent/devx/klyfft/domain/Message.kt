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

data class Route(val distance: Long,
                 val duration: Long,
                 val steps: List<Steps>,
                 val geometry: String,
                 val summary: String?,
                 var legs: List<Route>,
                 val weight: Long,
                 val weight_name: String)

data class Steps(val distance: Long,
                 val duration: Long,
                 val way_name: String,
                 val mode: String,
                 val direction: String,
                 val heading: Long,
                 val maneuver: Instruction)

data class Instruction(val instruction: String,
                       val type: String,
                       val location: List<Double>,
                       val bearing_after: Int,
                       val bearing_before: Int,
                       val modifier: String?)

data class Location(val type: String,
                    val coordinates: List<Double>) 


