package chatt.busbus.backend

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.file
import io.ktor.features.CallLogging
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("Application")

val database: MongoDatabase by lazy {
    KMongo.createClient("localhost:27017").getDatabase("busbus")
}

@Suppress("unused")
fun Application.main() {

    initDatabase()

    install(CallLogging)
    install(Routing) {

        get("/hello") {
            call.respond("hello world! xxx")
        }

        // serve frontend as a single-page-app
        file("frontend.css", "build/web/frontend.css")
        file("frontend.js", "build/web/frontend.js")
        file("frontend.js.map", "build/web/frontend.js")
        file("{...}", "build/web/frontend.html")

    }

}

fun initDatabase() {
    logger.info("Loading bus data")

    val nextBusClient = NextBusClient()
    val agencyColl = database.getCollection<Agency>().apply { drop() }
    val routeColl = database.getCollection<Route>().apply { drop() }
    val stopColl = database.getCollection<Stop>().apply { drop() }
    stopColl.createIndex(Indexes.geo2dsphere("location"))

    val agencies = nextBusClient.getAgencies()
    val sfMuni = agencies.first { it.tag == "sf-muni" }
    logger.info("Inserting ${agencies.size} agencies")
    agencyColl.insertMany(agencies)

    val routes = nextBusClient.getRoutes(sfMuni)
    logger.info("Inserting ${routes.size} routes")
    routeColl.insertMany(routes)

    val stops = nextBusClient.getStops(sfMuni)
    logger.info("Inserting ${stops.size} stops")
    stopColl.insertMany(stops)
}

fun main(args: Array<String>) {
    initDatabase()
}