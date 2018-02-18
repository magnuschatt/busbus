package chatt.busbus.backend.mongo

import chatt.busbus.backend.busdata.BusDatabase
import chatt.busbus.common.BusAgency
import chatt.busbus.common.BusRoute
import chatt.busbus.common.BusStop
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

class MongoDatabase : BusDatabase {

    private val mongoUri: MongoClientURI = MongoClientURI(System.getenv("MONGODB_URI"))
    private val database: MongoDatabase = KMongo.createClient(mongoUri).getDatabase(mongoUri.database)
    private val agencyCollection = database.getCollection<MongoSchema.Agency>()
    private val routeCollection = database.getCollection<MongoSchema.Route>()
    private val stopCollection = database.getCollection<MongoSchema.Stop>()

    override fun init() {
        agencyCollection.drop()
        routeCollection.drop()
        stopCollection.drop()
        stopCollection.createIndex(Indexes.geo2dsphere("location"))
    }

    override fun isEmpty(): Boolean {
        return stopCollection.count() == 0L
    }

    override fun insertAgencies(agencies: List<BusAgency>) {
        val docs = agencies.map { it.toMongo() }
        agencyCollection.insertMany(docs)
    }

    override fun insertRoutes(routes: List<BusRoute>) {
        val docs = routes.map { it.toMongo() }
        routeCollection.insertMany(docs)
    }

    override fun insertStops(stops: List<BusStop>) {
        val docs = stops.map { it.toMongo() }
        stopCollection.insertMany(docs)
    }

    override fun findNearbyStops(latitude: Double, longitude: Double, maxDistance: Double, limit: Int): List<BusStop> {
        val point = Point(Position(longitude, latitude)) // lat & lon is flipped in Mongo!
        val filter = Filters.near("location", point, maxDistance, 0.0)
        val stops = stopCollection.find(filter).limit(limit).toList()
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
            location = MongoSchema.GeoLocation(latitude, longitude)
    )

    // Mapper function: mongo -> common
    private fun MongoSchema.Stop.toCommon() = BusStop(
            tag = tag,
            title = title,
            agencyTag = agencyTag,
            routeTag = routeTag,
            latitude = location.latitude(),
            longitude = location.longitude()
    )


}
















































