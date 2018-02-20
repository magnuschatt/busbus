package chatt.busbus.backend.busdata

import chatt.busbus.backend.mongo.MongoBusDatabase
import chatt.busbus.common.BusDepartureInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Responsible for all bus data jobs.
 * Delegates tasks to BusDatabase.kt and NextBusClient.kt depending on what is needed.
 */
class BusDataService(forceLoadBusData: Boolean = false) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val nextBusClient = NextBusClient()
    private val database: BusDatabase = MongoBusDatabase()

    init {
        val emptyDatabase = database.isEmpty()

        if (forceLoadBusData || emptyDatabase) {
            logger.info("Loading bus data (forceLoadBusData=$forceLoadBusData, emptyDatabase=$emptyDatabase")
            loadBusData()
        }
    }

    private fun loadBusData() {
        database.init()

        // for now we only want to work with sf-muni
        val agencies = nextBusClient.getAgencies().filter { it.tag == "sf-muni" }
        database.insertAgencies(agencies)

        val routes = nextBusClient.getRoutes(agencies)
        database.insertRoutes(routes)

        val stops = nextBusClient.getStops(routes)
        database.insertStops(stops)
    }

    fun getNearbyDepartureInfo(latitude: Double, longitude: Double, maxDistance: Double, limit: Int): Any {
        val stops = database.findNearbyStops(latitude, longitude, maxDistance, limit)
        val predictions = nextBusClient.getPredictions(stops)
        return BusDepartureInfo(stops, predictions)
    }

}