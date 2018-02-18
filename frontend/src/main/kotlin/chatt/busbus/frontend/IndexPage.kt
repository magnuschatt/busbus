package chatt.busbus.frontend

import chatt.busbus.common.BackendUrls
import chatt.busbus.common.BusDepartureInfo
import chatt.kotlinspa.Http
import chatt.kotlinspa.Page
import kotlinx.html.dom.append
import kotlinx.html.js.h1
import kotlinx.serialization.json.JSON

val index: Page = Page.create("/") {
    append {
        h1 {
            +"Bus Departures Near You"
        }

        GeoLocation.getCurrentPosition { pos ->
            println(pos)
            Http.get(BackendUrls.departuresNearby(pos.latitude, pos.longitude, 1000.0)) {
                val predictionList = JSON.parse<BusDepartureInfo>(it.responseText)
                println(predictionList)
            }
        }

    }

}