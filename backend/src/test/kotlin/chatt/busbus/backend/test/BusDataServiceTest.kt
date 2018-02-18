package chatt.busbus.backend.test

import chatt.busbus.backend.busdata.BusDataService
import org.junit.Assert.assertFalse
import org.junit.Test

class BusDataServiceTest {

    private val busDataService = BusDataService()

    private val latitude = 37.80296
    private val longitude = -122.40103
    private val distance = 1000000.0 // meters

    private val stops by lazy {
        busDataService.getStopsNearby(latitude, longitude, distance, 100)
    }

    @Test
    fun smokeTest() {
        assertFalse(stops.isEmpty())

        val predictions = busDataService.getPredictions(stops)
        assertFalse(predictions.isEmpty())
    }

}