package chatt.busbus.backend.busdata

import chatt.busbus.backend.mongo.MongoDatabase
import chatt.busbus.common.BusPrediction
import chatt.busbus.common.BusStop
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BusDataService(forceLoadBusData: Boolean = true) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val nextBusClient = NextBusClient()
    private val database: BusDatabase = MongoDatabase()

    init {
        if (forceLoadBusData || database.isEmpty()) {
            loadBusData()
        }
    }

    private fun loadBusData() {
        logger.info("Loading bus data")
        database.init()

        // for now we only want to work with sf-muni
        val agencies = nextBusClient.getAgencies().filter { it.tag == "sf-muni" }
        logger.info("Inserting ${agencies.size} agencies")
        database.insertAgencies(agencies)

        val routes = nextBusClient.getRoutes(agencies)
        logger.info("Inserting ${routes.size} routes")
        database.insertRoutes(routes)

        val stops = nextBusClient.getStops(routes)
        logger.info("Inserting ${stops.size} stops")
        database.insertStops(stops)
    }

    fun getStopsNearby(latitude: Double, longitude: Double, maxDistance: Double, limit: Int = 10): List<BusStop> {
        return database.findNearbyStops(latitude, longitude, maxDistance, limit)
    }

    fun getPredictions(stops: List<BusStop>): List<BusPrediction> {
        return nextBusClient.getPredictions(stops)
    }

}