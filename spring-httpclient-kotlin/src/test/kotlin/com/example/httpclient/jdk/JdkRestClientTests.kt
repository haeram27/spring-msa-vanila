package com.example.httpclient.jdk

import com.example.httpclient.restclient.jdk.RestClient
import com.example.httpclient.restclient.jdk.RestClient.HttpMethod
import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JdkRestClientTests {
    private val log = KotlinLogging.logger {}
    private val client = RestClient

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Test
    fun head() {
        val url = "http://localhost:8181/api/pagenation/servers/count"
        val response = client.send(HttpMethod.HEAD, url, null, "{}")
        if (response.is2xxSuccessful && response.body.isNotEmpty()) {
            log.info { "Successfully received response with status body=\n${String(response.body)}" }
        } else {
            log.error { "Failed to receive successful response." }
        }
    }

    @Test
    fun count() {
        val url = "http://localhost:8181/api/pagenation/servers/count"
        val response = client.send(HttpMethod.POST, url, null, "{}")
        if (response.is2xxSuccessful && response.body.isNotEmpty()) {
            log.info { "Successfully received response with status body=\n${String(response.body)}" }
        } else {
            log.error { "Failed to receive successful response." }
        }
    }

    @Test
    fun pagenation() {
        val countUrl = "http://localhost:8181/api/pagenation/servers/count"
        val serverUrl = "http://localhost:8181/api/pagenation/servers"

        val response = client.send(HttpMethod.POST, countUrl, null, "{}")
        log.info { "response=\n${String(response.body)}" }

        val totalCount = try {
            val count = jsonMapper.readTree(response.body).at("/body/totalCount").asInt()
            log.info { "totalCount=$count" }
            count
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to parse response" }
            return
        }

        val requestBody = """
        {
            "pageNumber": %d,
            "pageSize": %d
        }
        """.trimIndent()

        val results = client.postAllPages(serverUrl, null, requestBody, totalCount, totalCount / 5, 3000)
        val count = results.values.count { it.isNotEmpty() }

        log.info { "received $count pages of servers" }
        if (count <= 0) {
            log.error { "can NOT receive any server list" }
            return
        }

        results.forEach { (k, v) ->
            if (v.isNotEmpty()) {
                log.info { "page=$k, body=\n${String(v)}" }
            } else {
                log.warn { "page=$k, body is empty" }
            }
        }
    }
}
