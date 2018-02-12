package chatt.busbus

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

@Suppress("unused")
fun Application.main() {

    install(CallLogging)

    install(Routing) {

        get("/hello") {
            call.respond("hello world! xxx")
        }

    }

}