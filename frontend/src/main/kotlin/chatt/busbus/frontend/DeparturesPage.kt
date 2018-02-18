package chatt.busbus.frontend

import chatt.busbus.common.*
import chatt.kotlinspa.Http
import chatt.kotlinspa.Page
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.js.*
import kotlinx.serialization.json.JSON
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.js.Date
import kotlin.math.roundToInt

val departuresPage: Page = Page.create("/") {
    append {
        h1 {
            +"Bus Departures Near You"
        }

        GeoLocation.getCurrentPosition { userPosition ->
            Http.get(BackendUrls.departuresNearby(userPosition.latitude, userPosition.longitude, 1000.0)) {

                val departureInfo = JSON.parse<BusDepartureInfo>(it.responseText)
                val stops: Map<String, BusStop> = departureInfo.stops.associateBy { it.title }

                // group predictions by stop (throw exc if stop is missing)
                // sort entries by distance from user
                // finally for each stop: append HTML info box
                departureInfo.predictions
                        .groupBy { stops[it.stopTitle]!! }
                        .map { Pair(it.key, it.value) }
                        .sortedBy { (stop, _) -> GeoLocation.distance(stop.position, userPosition)}
                        .forEach { (stop, predictions) -> appendStopInfoBox(stop, predictions, userPosition) }

                // show info if no predictions were found.
                if (departureInfo.predictions.isEmpty()) {
                    val manualGeoLink = "https://chrome.google.com/webstore/detail/manual-geolocation/jpiefjlgcjmciajdcinaejedejjfjgki"
                    p { +"No departures found near you." }
                    p { +"Departures are only shown when you are in San Francisco."}
                    p { +"If you want to test this app consider downloading chrome extension:" }
                    p { a(manualGeoLink) { +"Manual Geolocation" } }
                }
            }
        }
    }
}

fun TagConsumer<HTMLElement>.appendStopInfoBox(stop: BusStop,
                                               predictions: List<BusPrediction>,
                                               userPosition: Position) {
    div(classes = "stopInfoBox") {

        h3 {
            val mapsLink = "https://www.google.com/maps/?q=${stop.position.latitude},${stop.position.longitude}"
            a(href = mapsLink) { +stop.title }
            val distance = GeoLocation.distance(stop.position, userPosition)
            span(classes = "away") { +" (${distance.roundToInt()} meters from you)" }
        }

        predictions.forEach { appendDepartureInfo(it) }
    }
}

fun TagConsumer<HTMLElement>.appendDepartureInfo(prediction: BusPrediction) {
    p {
        span(classes = "circled") { +prediction.routeTag }
        prediction.directions.forEach { direction ->
            +" ${direction.title}"
            appendDepartureTime(direction.seconds)
        }
    }
}

fun TagConsumer<HTMLElement>.appendDepartureTime(departureTimes: List<Int>) {
    span(classes = "departs") {
        val timeElement = b { +"" }
        val pageLoadTime = Date().getTime()
        updateDepartureTime(timeElement, departureTimes, pageLoadTime)
        window.setInterval({ updateDepartureTime(timeElement, departureTimes, pageLoadTime) }, 1000)
    }
}

fun updateDepartureTime(timeElement: HTMLElement, departureTimes: List<Int>, pageLoadTime: Double) {

    var next = 0
    for (departureTime in departureTimes) {
        next = departureTime
        val now = Date().getTime()
        val secondsPassed = ((now - pageLoadTime) / 1000).roundToInt()
        next -= secondsPassed
        if (next > 0) break
    }

    val hours = next / 3600
    val minutes = (next % 3600) / 60
    val seconds = next % 60

    val display = when {
        hours > 0 -> "$hours hours $minutes minutes"
        minutes > 0 -> "$minutes minutes"
        else -> "$seconds seconds"
    }

    timeElement.innerText = display
}
