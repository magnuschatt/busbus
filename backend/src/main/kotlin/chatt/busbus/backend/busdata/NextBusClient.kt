package chatt.busbus.backend.busdata

import chatt.busbus.common.BusAgency
import chatt.busbus.common.BusPrediction
import chatt.busbus.common.BusRoute
import chatt.busbus.common.BusStop
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.json.XML
import java.net.URL

/**
 * Responsible for fetching raw data from the NextBusXMLFeed REST interface and for
 * converting it to our local data model (POJOs) containing only relevant data.
 */
class NextBusClient {

    private val baseCommandUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command="
    private val agencyListUrl = baseCommandUrl + "agencyList"
    private val routeListUrl = baseCommandUrl + "routeList&a=%s"
    private val routeConfigUrl = baseCommandUrl + "routeConfig&a=%s&r=%s&terse"
    private val predictionsUrl = baseCommandUrl + "predictionsForMultiStops&a=%s"
    private val jsonMapper = ObjectMapper().apply { registerKotlinModule() }

    fun getAgencies(): List<BusAgency> {
        val json = readFromUrl(agencyListUrl)
        val array = json.node("body").node("agency")
        return array.map {
            BusAgency(
                    tag = it.string("tag"),
                    title = it.string("title")
            )
        }
    }

    fun getRoutes(agencies: List<BusAgency>) = runBlocking {
        return@runBlocking agencies
                .map { route -> async { getRoutes(route) } }
                .flatMap { it.await() }
    }

    fun getRoutes(agency: BusAgency): List<BusRoute> {
        val url = String.format(routeListUrl, agency.tag)
        val json = readFromUrl(url)
        val array = json.node("body").node("route")
        return array.map {
            BusRoute(
                    tag = it.string("tag"),
                    agencyTag = agency.tag,
                    title = it.string("title")
            )
        }
    }

    fun getStops(route: BusRoute): List<BusStop> {
        val url = String.format(routeConfigUrl, route.agencyTag, route.tag)
        val json = readFromUrl(url)
        val array = json.node("body").node("route").node("stop")
        return array.map {
            BusStop(
                    tag = it.string("tag"),
                    title = it.string("title"),
                    agencyTag = route.agencyTag,
                    routeTag = route.tag,
                    latitude = it.double("lat"),
                    longitude = it.double("lon")
            )
        }
    }

    fun getStops(agency: BusAgency): List<BusStop> {
        return getStops(getRoutes(agency))
    }

    fun getStops(routes: List<BusRoute>): List<BusStop> = runBlocking {
        return@runBlocking routes
                .map { route -> async { getStops(route) } }
                .flatMap { it.await() }
    }

    fun getPredictions(stopList: List<BusStop>): List<BusPrediction> = runBlocking {
        val result = mutableListOf<Deferred<List<BusPrediction>>>()

        // grouping stops by agency to be able to correctly call NextBusXMLFeed
        stopList.groupBy(BusStop::agencyTag).forEach { agencyTag, stops ->

            // up to 150 stops per call to predictionsForMultiStops is allowed
            stops.chunked(150).forEach { stops150 ->

                // starting jobs asynchronously and adding them to result list
                val predictions = async {
                    val stopsString = stops150.joinToString("") { "&stops=${it.routeTag}|${it.tag}" }
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

    private fun extractPredictions(json: JsonNode): List<BusPrediction> {
        val result = mutableListOf<BusPrediction>()

        val array = json.node("body").array("predictions")
        array.filter { it.has("direction") }.forEach { prediction ->
            val stopTitle = prediction.string("stopTitle")
            val routeTag = prediction.string("routeTag")
            val routeTitle = prediction.string("routeTitle")

            prediction.array("direction").forEach { direction ->
                val directionTitle = direction.string("title")
                val seconds = direction.array("prediction").map { it.int("seconds") }
                result.add(BusPrediction(stopTitle, routeTag, routeTitle, directionTitle, seconds))
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