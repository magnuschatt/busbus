package chatt.busbus.common

import kotlinx.serialization.Serializable

@Serializable
data class BusPrediction(val stopTitle: String,
                         val routeTag: String,
                         val routeTitle: String,
                         val directionTitle: String,
                         val seconds: List<Int>)