package chatt.busbus.backend.busdata

import org.bson.codecs.pojo.annotations.BsonId

data class Agency(@BsonId val tag: String,
                  val title: String,
                  val regionTitle: String)

data class Route(@BsonId val tag: String,
                 val agencyTag: String,
                 val title: String)

data class Stop(val tag: String,
                val agencyTag: String,
                val routeTag: String,
                val title: String,
                val location: GeoLocation) {
    @BsonId
    val id: String = "$routeTag|$tag"
}

data class Prediction(val stopTitle: String,
                      val routeTag: String,
                      val dirTitle: String,
                      val seconds: List<Int>)

@Suppress("unused") // Used by MongoDB
class GeoLocation(latitude: Double, longitude: Double) {
    val type = "Point"
    val coordinates = listOf(longitude, latitude)
}