package chatt.busbus.backend.mongo

import chatt.busbus.backend.busdata.BusDatabase
import chatt.busbus.common.BusAgency
import chatt.busbus.common.BusRoute
import chatt.busbus.common.BusStop
import chatt.busbus.common.Position
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.geojson.Point
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

/**
 * Mongo implementation of the BusDatabase interface.
 */
class MongoBusDatabase : BusDatabase {

    private val logger = KotlinLogging.logger {}
    private val mongoUri: MongoClientURI = MongoClientURI(System.getenv("MONGODB_URI"))
    private val database: MongoDatabase = KMongo.createClient(mongoUri).getDatabase(mongoUri.database)
    private val agencyCollection = database.getCollection<MongoSchema.Agency>()
    private val routeCollection = database.getCollection<MongoSchema.Route>()
    private val stopCollection = database.getCollection<MongoSchema.Stop>()
    private val positionFieldName = MongoSchema.Stop::position.name

    override fun init() {
        agencyCollection.drop()
        routeCollection.drop()
        stopCollection.drop()
        stopCollection.createIndex(Indexes.geo2dsphere(positionFieldName))
    }

    override fun isEmpty(): Boolean {
        return stopCollection.count() == 0L
    }

    override fun insertAgencies(agencies: List<BusAgency>) {
        logger.info { "Inserting ${agencies.size} agencies" }

        val docs = agencies.map { it.toMongo() }
        agencyCollection.insertMany(docs)
    }

    override fun insertRoutes(routes: List<BusRoute>) {
        logger.info { "Inserting ${routes.size} routes" }

        val docs = routes.map { it.toMongo() }
        routeCollection.insertMany(docs)
    }

    override fun insertStops(stops: List<BusStop>) {
        logger.info { "Inserting ${stops.size} stops" }

        val docs = stops.map { it.toMongo() }
        stopCollection.insertMany(docs)
    }

    override fun findNearbyStops(latitude: Double, longitude: Double, maxDistance: Double, limit: Int): List<BusStop> {
        logger.info { "Finding stops near lat=$latitude, lon=$longitude, maxDist=$maxDistance, limit=$limit" }

        val point = Point(com.mongodb.client.model.geojson.Position(longitude, latitude)) // lat & lon is flipped in Mongo!
        val filter = Filters.near(positionFieldName, point, maxDistance, 0.0)
        val stops = stopCollection.find(filter).limit(limit).toList()

        logger.info { "Found ${stops.size} stops" }
        return stops.map { it.toCommon() }
    }

    // Mapper function: common -> mongo
    private fun BusAgency.toMongo() = MongoSchema.Agency(
            tag = tag,
            title = title
    )

    // Mapper function: common -> mongo
    private fun BusRoute.toMongo() = MongoSchema.Route(
            tag = tag,
            agencyTag = agencyTag,
            title = title
    )

    // Mapper function: common -> mongo
    private fun BusStop.toMongo() = MongoSchema.Stop(
            tag = tag,
            title = title,
            agencyTag = agencyTag,
            routeTag = routeTag,
            position = MongoSchema.Point(position.latitude, position.longitude)
    )

    // Mapper function: mongo -> common
    private fun MongoSchema.Stop.toCommon() = BusStop(
            tag = tag,
            title = title,
            agencyTag = agencyTag,
            routeTag = routeTag,
            position = Position(position.latitude(), position.longitude())
    )


}
















































