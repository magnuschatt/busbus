package chatt.busbus.backend.busdata

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BusDataService(forceLoadBusData: Boolean = false) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val database: MongoDatabase = KMongo.createClient(System.getenv("MONGODB_URI")).getDatabase("busbus")
    private val nextBusClient = NextBusClient()
    private val agencyColl = database.getCollection<Agency>()
    private val routeColl = database.getCollection<Route>()
    private val stopColl = database.getCollection<Stop>()

    init {
        if (forceLoadBusData || stopColl.count() == 0L) {
            loadBusData()
        }
    }

    private fun loadBusData() {
        logger.info("Loading bus data")

        agencyColl.drop()
        routeColl.drop()
        stopColl.drop()
        stopColl.createIndex(Indexes.geo2dsphere("location"))

        // Loading agencies
        val agencies = nextBusClient.getAgencies()
        val sfMuni = agencies.first { it.tag == "sf-muni" }
        logger.info("Inserting ${agencies.size} agencies")
        agencyColl.insertMany(agencies)

        // Loading routes
        val routes = nextBusClient.getRoutes(sfMuni)
        logger.info("Inserting ${routes.size} routes")
        routeColl.insertMany(routes)

        // Loading stops
        val stops = nextBusClient.getStops(sfMuni)
        logger.info("Inserting ${stops.size} stops")
        stopColl.insertMany(stops)
    }

    fun getStopsNearby(latitude: Double, longitude: Double, maxDistance: Double, limit: Int = 10): List<Stop> {
        val point = Point(Position(longitude, latitude)) // lat & lon is flipped in Mongo!
        val filter = Filters.near("location", point, maxDistance, 0.0)
        return stopColl.find(filter).limit(limit).toList()
    }

    fun getPredictions(stops: List<Stop>): List<Prediction> {
        return nextBusClient.getPredictions(stops)
    }

}