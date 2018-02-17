package chatt.busbus.backend.test

import chatt.busbus.backend.busdata.BusDataService
import org.junit.Assert.assertFalse
import org.junit.Ignore
import org.junit.Test

class BusDataServiceTest {

    private val busDataService = BusDataService()

    @Test @Ignore
    fun smokeTest() {
        val latitude = 37.80296
        val longitude = -122.40103
        val distance = 1000.0 // meters

        val stops = busDataService.getStopsNearby(latitude, longitude, distance)
        assertFalse(stops.isEmpty())

        val predictions = busDataService.getPredictions(stops)
        assertFalse(predictions.isEmpty())
    }

}