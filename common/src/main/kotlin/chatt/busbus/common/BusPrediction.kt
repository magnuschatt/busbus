package chatt.busbus.common

import kotlinx.serialization.Serializable

@Serializable
data class BusPrediction(val stopTitle: String,
                         val routeTag: String,
                         val routeTitle: String,
                         val directions: List<Direction>) {
    @Serializable
    data class Direction(val title: String,
                         val seconds: List<Int>)
}