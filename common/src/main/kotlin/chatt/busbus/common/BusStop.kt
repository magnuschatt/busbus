package chatt.busbus.common

import kotlinx.serialization.Serializable

@Serializable
data class BusStop(val tag: String,
                   val title: String,
                   val agencyTag: String,
                   val routeTag: String,
                   val position: Position)