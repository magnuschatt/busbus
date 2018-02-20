package chatt.busbus.backend.busdata

import chatt.busbus.common.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import org.json.XML
import java.net.URL

/**
 * Responsible for fetching raw data from the NextBusXMLFeed REST interface and for
 * converting it to our local data model (POJOs) containing only relevant data.
 */
class NextBusClient {

    private val logger = KotlinLogging.logger {}
    private val baseCommandUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command="
    private val agencyListUrl = baseCommandUrl + "agencyList"
    private val routeListUrl = baseCommandUrl + "routeList&a=%s"
    private val routeConfigUrl = baseCommandUrl + "routeConfig&a=%s&r=%s&terse"
    private val predictionsUrl = baseCommandUrl + "predictionsForMultiStops&a=%s"
    private val jsonMapper = ObjectMapper().apply { registerKotlinModule() }

    fun getAgencies(): List<BusAgency> {
        logger.debug { "Fetching agencies" }

        val json = readFromUrl(agencyListUrl)
        val array = json.node("body").node("agency")
        val agencies = array.map {
            BusAgency(
                    tag = it.string("tag"),
                    title = it.string("title")
            )
        }

        logger.info { "Fetched ${agencies.size} agencies from '$agencyListUrl'" }
        logger.debug { "Agencies fetched: $agencies" }
        return agencies
    }

    fun getRoutes(agencies: List<BusAgency>) = runBlocking {
        return@runBlocking agencies
                .map { route -> async { getRoutes(route) } }
                .flatMap { it.await() }
    }

    fun getRoutes(agency: BusAgency): List<BusRoute> {
        logger.debug { "Fetching routes for $agency" }

        val url = String.format(routeListUrl, agency.tag)
        val json = readFromUrl(url)
        val array = json.node("body").node("route")
        val routes = array.map {
            BusRoute(
                    tag = it.string("tag"),
                    agencyTag = agency.tag,
                    title = it.string("title")
            )
        }

        logger.info { "Fetched ${routes.size} routes from '$url'" }
        logger.debug { "Routes fetched: $routes" }
        return routes
    }

    fun getStops(route: BusRoute): List<BusStop> {
        logger.debug { "Fetching stops for $route" }

        val url = String.format(routeConfigUrl, route.agencyTag, route.tag)
        val json = readFromUrl(url)
        val array = json.node("body").node("route").node("stop")
        val stops = array.map {
            BusStop(
                    tag = it.string("tag"),
                    title = it.string("title"),
                    agencyTag = route.agencyTag,
                    routeTag = route.tag,
                    position = Position(it.double("lat"), it.double("lon"))
            )
        }

        logger.info { "Fetched ${stops.size} stops from '$url'" }
        logger.debug { "Stops fetched: $stops" }
        return stops
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
        logger.debug { "Fetching predictions from ${stopList.size} stops" }

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
                    val predictions = extractPredictions(json)

                    logger.debug { "Fetched ${predictions.size} predictions from '$url': $predictions" }
                    return@async predictions
                }

                result.add(predictions)
            }
        }

        // waiting for jobs to finish
        val predictions = result.flatMap { it.await() }
        logger.info { "Fetched ${predictions.size} predictions from ${stopList.size} stops" }
        return@runBlocking predictions
    }

    private fun extractPredictions(json: JsonNode): List<BusPrediction> {
        val result = mutableListOf<BusPrediction>()

        val array = json.node("body").array("predictions")
        array.filter { it.has("direction") }.forEach { prediction ->
            val stopTitle = prediction.string("stopTitle")
            val routeTag = prediction.string("routeTag")
            val routeTitle = prediction.string("routeTitle")

            val directions = prediction.array("direction").map { direction ->
                val directionTitle = direction.string("title")
                val seconds = direction.array("prediction").map { it.int("seconds") }
                BusPrediction.Direction(directionTitle, seconds)
            }

            result.add(BusPrediction(stopTitle, routeTag, routeTitle, directions))
        }

        return result
    }

    private fun readFromUrl(url: String): JsonNode {
        // bug in jackson XML reader make me go through org.json

        logger.debug { "Fetching bus data from: $url" }
        val xml = URL(url).openStream().bufferedReader().readText()
        logger.trace { "URL '$url' responded: '$xml'" }

        val json = XML.toJSONObject(xml).toString()
        val node = jsonMapper.readTree(json)

        logger.debug { "URL '$url' responded (xml converted to json): $node" }
        return node
    }

    private fun unexpectedXml(key: String, expectedType: String, node: JsonNode): Nothing {
        val msg = "Unexpected NextBus XML response: Expected type='$expectedType' at key='$key' in: $node"
        logger.error { msg }
        throw IllegalStateException(msg)
    }

    private fun JsonNode.array(key: String) = this.node(key).let { if (it.isArray) it else listOf(it) }
    private fun JsonNode.node(key: String) = this.get(key) ?: unexpectedXml(key, "node", this)
    private fun JsonNode.string(key: String) = this.get(key)?.asText() ?: unexpectedXml(key, "string", this)
    private fun JsonNode.double(key: String) = this.get(key)?.asDouble() ?: unexpectedXml(key, "string", this)
    private fun JsonNode.int(key: String) = this.get(key)?.asInt() ?: unexpectedXml(key, "string", this)
}