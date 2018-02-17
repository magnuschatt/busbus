package chatt.busbus.frontend

data class Prediction(val stopTitle: String,
                      val routeTag: String,
                      val dirTitle: String,
                      val seconds: List<Int>)