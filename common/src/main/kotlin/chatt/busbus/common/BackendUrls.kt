package chatt.busbus.common

object BackendUrls {

    const val departuresNearby = "backend/departures/nearby"

    fun departuresNearby(latitude: Double, longitude: Double, maxDistance: Double): String {
        return "$departuresNearby?lat=$latitude&lon=$longitude&dist=$maxDistance"
    }

}