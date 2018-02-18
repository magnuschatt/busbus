package chatt.busbus.frontend

import chatt.busbus.common.Position
import kotlin.browser.window

object GeoLocation {

    fun getCurrentPosition(then: (Position) -> Unit) {
        window.navigator.asDynamic().geolocation.getCurrentPosition { pos ->
            val latitude = (pos.coords.latitude as Double)
            val longitude = (pos.coords.longitude as Double)
            then(Position(latitude, longitude))
        }
    }

}