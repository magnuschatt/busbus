package chatt.busbus.backend

import chatt.busbus.backend.busdata.BusDataService
import chatt.busbus.common.BackendUrls
import chatt.busbus.common.BusDepartureInfo
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.file
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

@Suppress("unused")
fun Application.main() {

    class ParameterException(val msg: String) : Exception(msg)

    install(StatusPages) {
        exception<ParameterException> {
            call.respond(HttpStatusCode.BadRequest, it.msg)
        }
    }

    install(ContentNegotiation) {
        jackson {} // Register Jackson as JSON parser
    }

    install(CallLogging)
    install(Routing) {

        val busDataService = BusDataService(forceLoadBusData = true)

        // expose endpoint for fetching departure predictions near given coordinate
        get(BackendUrls.departuresNearby) {
            val latitude = call.parameters["lat"]?.toDoubleOrNull()
            val longitude = call.parameters["lon"]?.toDoubleOrNull()
            val distance = call.parameters["dist"]?.toDoubleOrNull()

            if (latitude == null || longitude == null || distance == null) {
                throw ParameterException("Call must contain numeric parameters: lat, lon, dist")
            }

            val stops = busDataService.getStopsNearby(latitude, longitude, distance, 100)
            val predictions = busDataService.getPredictions(stops)
            val departureInfo = BusDepartureInfo(stops, predictions)
            call.respond(departureInfo)
        }

        // serve frontend as a single-page-app
        val webDir = "build/web"
        static("web") { files(webDir) }
        file("{...}", "$webDir/frontend.html")
    }

}
