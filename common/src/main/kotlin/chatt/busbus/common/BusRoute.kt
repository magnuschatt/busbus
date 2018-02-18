package chatt.busbus.common

import kotlinx.serialization.Serializable

@Serializable
class BusRoute(val tag: String,
               val agencyTag: String,
               val title: String)