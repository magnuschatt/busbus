package chatt.busbus.frontend

import chatt.busbus.common.BackendUrls
import chatt.busbus.common.BusDepartureInfo
import chatt.kotlinspa.Http
import chatt.kotlinspa.Page
import chatt.kotlinspa.Pages
import kotlinx.html.dom.append
import kotlinx.html.js.button
import kotlinx.html.js.h1
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.json.JSON

val index: Page = Page.create("/") {
    append {
        button {
            +"Home"
            onClickFunction = { Pages.renderCurrent() }
        }

        h1 {
            +"Index"
        }

        Http.get(BackendUrls.departuresNearby(37.80296, -122.40103, 100000.0)) {
            val predictionList = JSON.parse<BusDepartureInfo>(it.responseText)
            println(predictionList)
        }

    }

}