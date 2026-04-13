package com.example.httpclient.spring

import com.example.EvaluatedTimeTests
import tools.jackson.core.JacksonException
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URI
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@SpringBootTest
class SpringRestTemplateSyncTests : EvaluatedTimeTests() {
    private val log = KotlinLogging.logger {}

    @Autowired
    @Qualifier("jdkClientTrustAllRestTemplate")
    private lateinit var restTemplate: RestTemplate

    @Autowired
    @Qualifier("restClientJsonMapper")
    private lateinit var mapper: JsonMapper

    companion object {
        private const val API_URL_DOMAIN = "jsonplaceholder.typicode.com"
        private const val API_URL_PATH = "/todos/1"
        private const val TEST_BEARER_AUTH_TOKEN = ""
    }

    fun sendResponseMapToJsonNode(method: HttpMethod, uri: URI, httpEntity: HttpEntity<Void>): JsonNode {
        var responseBody: JsonNode = mapper.createObjectNode()

        try {
            log.debug { "## request uri : $uri" }
            val responseEntity: ResponseEntity<JsonNode> = restTemplate.exchange(uri, method, httpEntity, JsonNode::class.java)

            log.debug { "Response Body: ${responseEntity.body}" }
            log.debug { "Status Code: ${responseEntity.statusCode}" }
            log.debug { "Headers: ${responseEntity.headers}" }

            when {
                responseEntity.statusCode == HttpStatus.OK -> {
                    val body = responseEntity.body
                    if (body == null || body.isEmpty) {
                        log.error { "Error: Response content type is NOT applicable" }
                    } else {
                        responseBody = body
                    }
                }

                responseEntity.statusCode == HttpStatus.NO_CONTENT -> {
                    log.error { "Error: No content" }
                }

                else -> {
                    log.error { "Error: Http Server Internal Error" }
                }
            }
        } catch (ex: RestClientResponseException) {
            log.error { "Error: ${ex.message}" }
            log.debug { "Status Code: ${ex.statusCode}" }
            log.debug { "Response Body: ${ex.responseBodyAsString}" }
            log.debug { "Headers: ${ex.responseHeaders}" }
        } catch (ex: ResourceAccessException) {
            log.error { "Error: ${ex.message}" }
        } catch (ex: Exception) {
            log.error { "Error: ${ex.message}" }
        }

        return responseBody
    }

    @Test
    fun getTestJsonNode() {
        val uri = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(API_URL_DOMAIN)
            .path(API_URL_PATH)
            .encode()
            .build()
            .toUri()

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        if (TEST_BEARER_AUTH_TOKEN.isNotEmpty()) {
            headers.setBearerAuth(TEST_BEARER_AUTH_TOKEN)
        }

        val httpEntity = HttpEntity<Void>(headers)
        val responseBody = sendResponseMapToJsonNode(HttpMethod.GET, uri, httpEntity)

        if (responseBody.size() > 0) {
            log.debug { "## response: ${responseBody.toPrettyString()}" }
        } else {
            log.error { "## empty response" }
        }
    }

    fun sendResponseMapToString(method: HttpMethod, uri: URI, httpEntity: HttpEntity<Void>): List<Map<String, Any?>> {
        var list: MutableList<Map<String, Any?>> = mutableListOf()

        try {
            log.debug { "## request uri : $uri" }
            val responseEntity: ResponseEntity<String> = restTemplate.exchange(uri, method, httpEntity, String::class.java)

            log.debug { "Response Body: ${responseEntity.body}" }
            log.debug { "Status Code: ${responseEntity.statusCode}" }
            log.debug { "Headers: ${responseEntity.headers}" }

            when {
                responseEntity.statusCode == HttpStatus.OK -> {
                    val body = responseEntity.body ?: ""
                    val responseBody = mapper.readTree(body)

                    if (responseBody.isObject) {
                        val node = responseBody as ObjectNode
                        val map: Map<String, Any?> = mapper.convertValue(node, object : TypeReference<Map<String, Any?>>() {})
                        list.add(map)
                    } else if (responseBody.isArray) {
                        val node = responseBody as ArrayNode
                        list = mapper.convertValue(node, object : TypeReference<MutableList<Map<String, Any?>>>() {})
                    } else {
                        log.error { "Error: Response content type is NOT applicable" }
                    }
                }

                responseEntity.statusCode == HttpStatus.NO_CONTENT -> {
                    log.error { "Error: No content" }
                }

                else -> {
                    log.error { "Error: Http Server Internal Error" }
                }
            }
        } catch (ex: RestClientResponseException) {
            log.error { "Error: ${ex.message}" }
            log.debug { "Status Code: ${ex.statusCode}" }
            log.debug { "Response Body: ${ex.responseBodyAsString}" }
            log.debug { "Headers: ${ex.responseHeaders}" }
        } catch (ex: ResourceAccessException) {
            log.error { "Error: ${ex.message}" }
        } catch (ex: Exception) {
            log.error { "Error: ${ex.message}" }
        }

        return list
    }

    @Test
    fun getTestMap() {
        val uri = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(API_URL_DOMAIN)
            .path(API_URL_PATH)
            .encode()
            .build()
            .toUri()

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        if (TEST_BEARER_AUTH_TOKEN.isNotEmpty()) {
            headers.setBearerAuth(TEST_BEARER_AUTH_TOKEN)
        }

        val httpEntity = HttpEntity<Void>(headers)
        val responseBody = sendResponseMapToString(HttpMethod.GET, uri, httpEntity)

        if (responseBody.isNotEmpty()) {
            try {
                log.debug { "## response: ${mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseBody)}" }
            } catch (_: JacksonException) {
                log.error { "## invalid response" }
            }
        } else {
            log.error { "## empty response" }
        }
    }
}
