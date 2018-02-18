package chatt.busbus.backend.mongo

import org.bson.codecs.pojo.annotations.BsonId

/**
 * This class defines the data structure of the mongo database collections.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // fields are used by mongo parser
object MongoSchema {

    data class Agency(@BsonId val tag: String,
                      val title: String)

    data class Route(@BsonId val tag: String,
                     val agencyTag: String,
                     val title: String)

    data class Stop(val tag: String,
                    val title: String,
                    val agencyTag: String,
                    val routeTag: String,
                    val location: GeoLocation) {
        @BsonId
        val id: String = "$agencyTag|$routeTag|$tag"
    }

    class GeoLocation(latitude: Double, longitude: Double) {
        val type = "Point"
        val coordinates = listOf(longitude, latitude) // lat & lon is flipped in Mongo!

        fun getLatitude() = coordinates[1]
        fun getLongitude() = coordinates[0]
    }

}