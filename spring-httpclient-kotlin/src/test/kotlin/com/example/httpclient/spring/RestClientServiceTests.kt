package com.example.httpclient.spring

import com.example.EvaluatedTimeTests
import com.example.httpclient.restclient.spring.RestClientService
import tools.jackson.databind.json.JsonMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod

@SpringBootTest
class RestClientServiceTests : EvaluatedTimeTests() {
    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Autowired
    private lateinit var client: RestClientService

    private val urlGetTodoOne = "https://jsonplaceholder.typicode.com/todos/1"
    private val urlPostJsonPlaceHolder = "https://jsonplaceholder.typicode.com/posts"

    data class RequestBody(
        val userId: Int,
        val id: Int,
        val title: String,
        val body: String,
    )

    @Test
    fun postLocalSerer() {
        log.info { "uri=$urlGetTodoOne" }

        client.sendAsync(
            HttpMethod.POST,
            "http://localhost:8181/api/post/echo",
            "passw@rd",
            RequestBody(10, 101, "foo", "bar"),
        ) { ok, body ->
            if (ok) {
                log.info { "success" }
                if (body != null) {
                    try {
                        log.info { jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body) }
                    } catch (e: Exception) {
                        log.error(e) { e.message ?: "failed to print body" }
                    }
                }
            } else {
                log.info { "failed" }
            }
        }
    }

    @Test
    fun postJsonPlaceHolder() {
        log.info { "uri=$urlGetTodoOne" }

        client.sendAsync(
            HttpMethod.POST,
            urlPostJsonPlaceHolder,
            "passw@rd",
            RequestBody(10, 101, "foo", "bar"),
        ) { ok, body ->
            if (ok) {
                log.info { "success" }
                if (body != null) {
                    try {
                        log.info { jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body) }
                    } catch (e: Exception) {
                        log.error(e) { e.message ?: "failed to print body" }
                    }
                }
            } else {
                log.info { "failed" }
            }
        }
    }
}
