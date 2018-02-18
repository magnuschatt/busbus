package chatt.busbus.frontend

import chatt.busbus.common.BackendUrls
import chatt.busbus.common.BusDepartureInfo
import chatt.kotlinspa.Http
import chatt.kotlinspa.Page
import kotlinx.html.dom.append
import kotlinx.html.js.b
import kotlinx.html.js.div
import kotlinx.html.js.h1
import kotlinx.html.js.p
import kotlinx.serialization.json.JSON

val index: Page = Page.create("/") {
    append {
        h1 {
            +"Departures Near You"
        }

        GeoLocation.getCurrentPosition { pos ->
            Http.get(BackendUrls.departuresNearby(pos.latitude, pos.longitude, 1000.0)) {
                val departureInfo = JSON.parse<BusDepartureInfo>(it.responseText)
                departureInfo.predictions.groupBy { it.stopTitle }.forEach { (stopTitle, _) ->
                    div(classes = "whitebox") {
                        p { b { +stopTitle } }
                    }
                }
            }
        }
    }

}