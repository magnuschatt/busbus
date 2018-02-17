package chatt.busbus.backend

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.json.XML
import java.net.URL

/**
 * Responsible for fetching raw data from the NextBusXMLFeed.
 * Converts the XML responses to JSON.
 */
class NextBusClient {

    // REST interface defined by the NextBusXMLFeed
    private val baseCommandUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command="
    private val agencyListUrl = baseCommandUrl + "agencyList"
    private val routeListUrl = baseCommandUrl + "routeList&a=%s"
    private val routeConfigUrl = baseCommandUrl + "routeConfig&a=%s&r=%s&terse"
    private val predictionsUrl = baseCommandUrl + "predictionsForMultiStops&a=%s"
    private val jsonMapper = ObjectMapper()

    fun getAgencies(): List<Agency> {
        val json = readFromUrl(agencyListUrl)
        val array = json.node("body").node("agency")
        return array.map {
            Agency(it.string("tag"), it.string("title"), it.string("regionTitle"))
        }
    }

    fun getRoutes(agency: Agency): List<Route> {
        val url = String.format(routeListUrl, agency.tag)
        val json = readFromUrl(url)
        val array = json.node("body").node("route")
        return array.map {
            Route(it.string("tag"), agency.tag, it.string("title"))
        }
    }

    fun getStops(route: Route): List<Stop> {
        val url = String.format(routeConfigUrl, route.agencyTag, route.tag)
        val json = readFromUrl(url)
        val array = json.node("body").node("route").node("stop")
        return array.map {
            val location = GeoLocation(it.double("lat"), it.double("lon"))
            Stop(it.string("tag"), route.agencyTag, route.tag, it.string("title"), location)
        }
    }

    fun getStops(agency: Agency): List<Stop> = runBlocking {
        return@runBlocking getRoutes(agency)
                .map { route -> async { getStops(route) } }
                .flatMap { it.await() }
    }

    fun getPredictions(stopList: List<Stop>): List<Prediction> {
        val result = mutableListOf<Prediction>()

        // grouping stops by agency to be able to correctly call NextBusXMLFeed
        stopList.groupBy(Stop::agencyTag).forEach { agencyTag, stops_ ->

            // up to 150 stops per call to predictionsForMultiStops is allowed
            stops_.chunked(150).forEach { stops ->
                val stopsString = stops.joinToString("") { "&stops=${it.routeTag}|${it.tag}" }
                val url = String.format(predictionsUrl, agencyTag) + stopsString
                val json = readFromUrl(url)
                val array = json.node("body").node("predictions")
                val predictions = array.filter { it.has("direction") }.map {
                    val stopTitle = it.string("stopTitle")
                    val routeTag = it.string("routeTag")
                    val dirObj = it.node("direction")
                    val dirTitle = dirObj.string("title")
                    val seconds = dirObj.node("prediction").map { it.int("seconds") }
                    Prediction(stopTitle, routeTag, dirTitle, seconds)
                }
                result.addAll(predictions)
            }
        }

        return result
    }

    private fun readFromUrl(url: String): JsonNode {
        // bug in jackson XML reader force me to parse it through org.json
        val xml = URL(url).openStream().bufferedReader().readText()
        val json = XML.toJSONObject(xml).toString()
        return jsonMapper.readTree(json)
    }

    private fun unexpectedXml() = IllegalStateException("Unexpected NextBus XML structure")
    private fun JsonNode.node(key: String) = this.get(key) ?: throw unexpectedXml()
    private fun JsonNode.string(key: String) = this.get(key)?.asText() ?: throw unexpectedXml()
    private fun JsonNode.double(key: String) = this.get(key)?.asDouble() ?: throw unexpectedXml()
    private fun JsonNode.int(key: String) = this.get(key)?.asInt() ?: throw unexpectedXml()
}