package chatt.busbus.backend

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.file
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

        // serve frontend as a single-page-app
        file("frontend.css", "build/web/frontend.css")
        file("frontend.js", "build/web/frontend.js")
        file("frontend.js.map", "build/web/frontend.js")
        file("{...}", "build/web/frontend.html")

    }

}