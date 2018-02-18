package chatt.busbus.backend.test

import chatt.busbus.backend.busdata.NextBusClient
import org.junit.Assert.assertFalse
import org.junit.Test

class NextBusClientTest {

    private val nextBusClient = NextBusClient()

    @Test
    fun smokeTest() {
        val agencies = nextBusClient.getAgencies()
        assertFalse(agencies.isEmpty())

        val sfMuni = agencies.first { it.tag == "sf-muni" }

        val routes = nextBusClient.getRoutes(sfMuni)
        assertFalse(routes.isEmpty())

        val stops = nextBusClient.getStops(sfMuni)
        assertFalse(stops.isEmpty())

        val predictions = nextBusClient.getPredictions(stops.take(200))
        assertFalse(predictions.isEmpty())

        assertFalse(nextBusClient.getStops(routes.first()).isEmpty())
    }

}