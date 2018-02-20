package chatt.busbus.backend.test

import chatt.busbus.backend.busdata.BusDataService
import org.junit.Assert.assertFalse
import org.junit.Test

class BusDataServiceTest {

    private val busDataService = BusDataService()

    private val latitude = 37.80296
    private val longitude = -122.40103
    private val distance = 1000000.0 // meters

    @Test
    fun smokeTest() {
        val (stops, predictions) = busDataService.getNearbyDepartureInfo(latitude, longitude, distance, 200)
        assertFalse(stops.isEmpty())
        assertFalse(predictions.isEmpty())
    }

}