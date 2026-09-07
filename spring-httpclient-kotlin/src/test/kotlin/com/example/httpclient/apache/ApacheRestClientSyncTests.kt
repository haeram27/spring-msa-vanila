package com.example.httpclient.apache

import com.example.EvaluatedTimeTests
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@SpringBootTest
class ApacheRestClientSyncTests : EvaluatedTimeTests() {
    private val log = KotlinLogging.logger {}

    @Autowired
    @Qualifier("apacheClientTrustAllRestClient")
    private lateinit var restClient: RestClient

    @Autowired
    @Qualifier("restClientJsonMapper")
    private lateinit var mapper: JsonMapper

    private val serverUrl1 = "https://jsonplaceholder.typicode.com/todos"

    @Test
    fun get() {
        log.info { "uri=$serverUrl1" }

        val responseBody: JsonNode = try {
            restClient.get()
                .uri(serverUrl1)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful) { _, response ->
                    val m = "${response.statusCode}, ${response.headers}"
                    log.info { m }
                }
                .onStatus(HttpStatusCode::isError) { _, response ->
                    val m = "${response.statusCode}, ${response.headers}"
                    log.error { m }
                }
                .body(JsonNode::class.java) ?: mapper.createObjectNode()
        } catch (e: Exception) {
            log.error(e) { e.message ?: "request failed" }
            return
        }

        if (responseBody.isEmpty) {
            log.error { "empty response" }
            return
        }

        log.info { "response:\n${responseBody.toPrettyString()}" }
    }

    data class RequestBody(
        val title: String,
        val body: String,
        val userId: Int,
    )

    @Test
    fun post() {
        val responseBody: JsonNode = try {
            restClient.post()
                .uri("https://jsonplaceholder.typicode.com/posts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(RequestBody("a", "b", 2))
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful) { request, response ->
                    log.info { "=== request ===============================================================" }
                    log.info { "URI: ${request.uri}" }
                    log.info { "Method: ${request.method}" }
                    log.info { "Headers: ${request.headers}" }
                    log.info { "=== response ===============================================================" }
                    log.info { "${response.statusCode}, ${response.headers}" }
                }
                .onStatus(HttpStatusCode::isError) { request, response ->
                    log.info { "=== request ===============================================================" }
                    log.info { "URI: ${request.uri}" }
                    log.info { "Method: ${request.method}" }
                    log.info { "Headers: ${request.headers}" }
                    log.info { "=== response ===============================================================" }
                    log.error { "${response.statusCode}, ${response.headers}" }
                }
                .body(JsonNode::class.java) ?: mapper.createObjectNode()
        } catch (e: Exception) {
            log.error(e) { e.message ?: "request failed" }
            return
        }

        if (responseBody.isEmpty) {
            log.error { "empty response" }
            return
        }

        log.info { "response:\n${responseBody.toPrettyString()}" }
    }
}
