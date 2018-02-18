package chatt.busbus.common

object BackendUrls {

    const val departures = "backend/departures"

    fun departures(latitude: Double, longitude: Double, maxDistance: Double): String {
        return "$departures?lat=$latitude&lon=$longitude&dist=$maxDistance"
    }

}