package chatt.busbus.frontend

import chatt.busbus.common.Position
import kotlin.browser.window
import kotlin.math.*

object GeoLocation {

    /**
     * Registers the current geo position of the user.
     * Runs supplied function when it is found.
     */
    fun getCurrentPosition(then: (Position) -> Unit) {
        window.navigator.asDynamic().geolocation.getCurrentPosition { pos ->
            val latitude = (pos.coords.latitude as Double)
            val longitude = (pos.coords.longitude as Double)
            then(Position(latitude, longitude))
        }
    }

    /**
     * Calculates distance in meters between two geo positions.
     * Grabbed from:
     * https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
     */
    fun distance(pos1: Position, pos2: Position): Double {
        val lat1 = pos1.latitude
        val lon1 = pos1.longitude
        val lat2 = pos2.latitude
        val lon2 = pos2.longitude

        fun deg2rad(deg: Double) = deg * (PI/180)

        val earthRadius = 6371000.0
        val dLat = deg2rad(lat2-lat1)
        val dLon = deg2rad(lon2-lon1)
        val a = sin(dLat/2) * sin(dLat/2) + cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * sin(dLon/2) * sin(dLon/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return earthRadius * c
    }

}