package chatt.busbus.backend.busdata

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.json.XML
import java.net.URL

/**
 * Responsible for fetching raw data from the NextBusXMLFeed REST interface.
 * Converts the XML responses to POJOs containing only relevant data.
 */
class NextBusClient {

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

    fun getStops(routes: List<Route>): List<Stop> = runBlocking {
        return@runBlocking routes
                .map { route -> async { getStops(route) } }
                .flatMap { it.await() }
    }

    fun getStops(agency: Agency): List<Stop> {
        return getStops(getRoutes(agency))
    }

    fun getPredictions(stopList: List<Stop>): List<Prediction> = runBlocking {
        val result = mutableListOf<Deferred<List<Prediction>>>()

        // grouping stops by agency to be able to correctly call NextBusXMLFeed
        stopList.groupBy(Stop::agencyTag).forEach { agencyTag, stops_ ->

            // up to 150 stops per call to predictionsForMultiStops is allowed
            stops_.chunked(150).forEach { stops ->

                // starting jobs asynchronously and adding them to result list
                val predictions = async {
                    val stopsString = stops.joinToString("") { "&stops=${it.routeTag}|${it.tag}" }
                    val url = String.format(predictionsUrl, agencyTag) + stopsString
                    val json = readFromUrl(url)
                    return@async extractPredictions(json)
                }

                result.add(predictions)
            }
        }

        // waiting for jobs to finish
        return@runBlocking result.flatMap { it.await() }
    }

    private fun extractPredictions(json: JsonNode): List<Prediction> {
        val result = mutableListOf<Prediction>()

        val array = json.node("body").array("predictions")
        array.filter { it.has("direction") }.forEach { prediction ->
            val stopTitle = prediction.string("stopTitle")
            val routeTag = prediction.string("routeTag")

            prediction.array("direction").forEach { direction ->
                val dirTitle = direction.string("title")
                val seconds = direction.array("prediction").map { it.int("seconds") }
                result.add(Prediction(stopTitle, routeTag, dirTitle, seconds))
            }
        }

        return result
    }

    private fun readFromUrl(url: String): JsonNode {
        // bug in jackson XML reader make me go through org.json
        val xml = URL(url).openStream().bufferedReader().readText()
        val json = XML.toJSONObject(xml).toString()
        return jsonMapper.readTree(json)
    }

    private fun unexpectedXml(key: String, expectedType: String, node: JsonNode): IllegalStateException {
        val msg = "Unexpected NextBus XML: Expected type='$expectedType' at key='$key' in: $node"
        return IllegalStateException(msg)
    }

    private fun JsonNode.array(key: String) = this.node(key).let { if (it.isArray) it else listOf(it) }
    private fun JsonNode.node(key: String) = this.get(key) ?: throw unexpectedXml(key, "node", this)
    private fun JsonNode.string(key: String) = this.get(key)?.asText() ?: throw unexpectedXml(key, "string", this)
    private fun JsonNode.double(key: String) = this.get(key)?.asDouble() ?: throw unexpectedXml(key, "string", this)
    private fun JsonNode.int(key: String) = this.get(key)?.asInt() ?: throw unexpectedXml(key, "string", this)
}