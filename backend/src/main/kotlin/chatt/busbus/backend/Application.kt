package chatt.busbus.backend

import chatt.busbus.backend.busdata.BusDataService
import chatt.busbus.common.BackendUrls
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
import org.slf4j.event.Level

@Suppress("unused")
fun Application.main() {

    install(ContentNegotiation) { jackson {} } // Register Jackson as JSON parser
    install(CallLogging) { level = Level.INFO }

    class ParameterException(val msg: String) : Exception(msg)
    install(StatusPages) {
        exception<ParameterException> { call.respond(HttpStatusCode.BadRequest, it.msg) }
    }

    install(Routing) {

        val busDataService = BusDataService()

        // expose endpoint for fetching departure predictions near given coordinate
        get(BackendUrls.departuresNearby) {
            val latitude = call.parameters["lat"]?.toDoubleOrNull()
            val longitude = call.parameters["lon"]?.toDoubleOrNull()
            val maxDistance = call.parameters["dist"]?.toDoubleOrNull()

            if (latitude == null || longitude == null || maxDistance == null) {
                throw ParameterException("Call must contain numeric parameters: lat, lon, dist")
            }

            val departureInfo = busDataService.getNearbyDepartureInfo(latitude, longitude, maxDistance, 100)
            call.respond(departureInfo)
        }

        // serve frontend as a single-page-app
        val webDir = "build/web"
        static("web") { files(webDir) }
        file("{...}", "$webDir/frontend.html")
    }

}
