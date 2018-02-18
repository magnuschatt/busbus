package chatt.busbus.common

import kotlinx.serialization.Serializable

@Serializable
data class BusAgency(val tag: String,
                     val title: String)