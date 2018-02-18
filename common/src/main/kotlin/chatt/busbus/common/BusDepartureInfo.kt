package chatt.busbus.common

import kotlinx.serialization.Serializable

@Serializable
data class BusDepartureInfo(val stops: List<BusStop>,
                            val predictions: List<BusPrediction>)