package chatt.busbus.backend.busdata

import chatt.busbus.common.BusAgency
import chatt.busbus.common.BusRoute
import chatt.busbus.common.BusStop

interface BusDatabase {

    fun init()
    fun insertAgencies(agencies: List<BusAgency>)
    fun insertRoutes(routes: List<BusRoute>)
    fun insertStops(stops: List<BusStop>)
    fun isEmpty(): Boolean
    fun findNearbyStops(latitude: Double, longitude: Double, maxDistance: Double, limit: Int): List<BusStop>

}