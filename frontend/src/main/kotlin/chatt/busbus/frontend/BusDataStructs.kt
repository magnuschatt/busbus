package chatt.busbus.frontend

import kotlinx.serialization.Serializable

@Serializable
data class Prediction(val stopTitle: String,
                      val routeTag: String,
                      val dirTitle: String,
                      val seconds: List<Int>)