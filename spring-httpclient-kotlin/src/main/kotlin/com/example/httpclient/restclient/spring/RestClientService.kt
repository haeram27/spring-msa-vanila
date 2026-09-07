package com.example.httpclient.restclient.spring

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpTimeoutException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient

@Service
class RestClientService(
    private val restClient: RestClient,
    private val jsonMapper: JsonMapper,
) {
    private val log = KotlinLogging.logger {}

    fun interface ResponseHandler {
        fun onReceived(is2xxSuccessful: Boolean, body: JsonNode?)
    }

    fun interface ResponseDetailHandler {
        fun onReceived(is2xxSuccessful: Boolean, request: HttpRequest?, response: ClientHttpResponse?)
    }

    class Response {
        var is2xxSuccessful: Boolean = false
        var body: JsonNode? = null
    }

    class ResponseDetail {
        var is2xxSuccessful: Boolean = false
        var request: HttpRequest? = null
        var closableResponse: ClientHttpResponse? = null
    }

    private fun getRootCause(throwable: Throwable): Throwable {
        var current = throwable
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }

    private fun hasCause(throwable: Throwable, type: Class<out Throwable>): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            if (type.isInstance(current)) {
                return true
            }
            if (current.cause === current) {
                break
            }
            current = current.cause
        }
        return false
    }

    private fun logResourceAccessException(method: HttpMethod, url: String, e: ResourceAccessException) {
        val rootCause = getRootCause(e)
        val errorType = rootCause.javaClass.simpleName
        val errorMessage = if (StringUtils.hasText(rootCause.message)) rootCause.message else "no message"

        if (hasCause(e, HttpConnectTimeoutException::class.java)) {
            log.error(e) {
                "restapi connect timeout error: method=$method, url=$url, type=$errorType, message=$errorMessage"
            }
            return
        }

        if (hasCause(e, ConnectException::class.java)) {
            log.error(e) {
                "restapi connection error: method=$method, url=$url, type=$errorType, message=$errorMessage"
            }
            return
        }

        if (hasCause(e, HttpTimeoutException::class.java) || hasCause(e, SocketTimeoutException::class.java)) {
            log.error(e) {
                "restapi timeout error: method=$method, url=$url, type=$errorType, message=$errorMessage"
            }
            return
        }

        if (hasCause(e, UnknownHostException::class.java)) {
            log.error(e) {
                "restapi dns error: method=$method, url=$url, type=$errorType, message=$errorMessage"
            }
            return
        }

        log.error(e) {
            "restapi io error: method=$method, url=$url, type=$errorType, message=$errorMessage"
        }
    }

    fun send(method: HttpMethod?, url: String, apikey: String?, body: Any?): Response {
        val httpResponse = Response()

        if (method == null) {
            log.error { "method is null" }
            return httpResponse
        }

        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            return httpResponse
        }

        val reqSpec = restClient.method(method)
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)

        if (body != null) {
            reqSpec.body(body)
        }

        if (StringUtils.hasText(apikey)) {
            reqSpec.header("Authorization", apikey!!)
        }

        try {
            log.debug {
                "send restapi request:\nurl: $url\nbody: ${if (body == null) "null" else jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body)}"
            }

            reqSpec.retrieve()
                .onStatus(HttpStatusCode::is1xxInformational) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    response.close()
                }
                .onStatus(HttpStatusCode::is2xxSuccessful) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    httpResponse.is2xxSuccessful = true
                    try {
                        response.use {
                            val respBody = jsonMapper.readValue(response.body.readAllBytes(), JsonNode::class.java)
                            httpResponse.body = respBody
                        }
                    } catch (e: IOException) {
                        log.error(e) { e.message ?: "failed to read successful response body" }
                    }
                }
                .onStatus(HttpStatusCode::is3xxRedirection) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    response.close()
                }
                .onStatus(HttpStatusCode::isError) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    try {
                        response.use {
                            log.error { "restapi error response body: ${String(response.body.readAllBytes())}" }
                        }
                    } catch (e: IOException) {
                        log.error(e) { e.message ?: "failed to read error response body" }
                    }
                }
                .toBodilessEntity()
        } catch (e: ResourceAccessException) {
            logResourceAccessException(method, url, e)
        } catch (e: Exception) {
            log.error(e) { e.message ?: "unexpected error while sending restapi request" }
        }

        return httpResponse
    }

    fun sendDetail(method: HttpMethod?, url: String, apikey: String?, body: Any?): ResponseDetail {
        val httpResponse = ResponseDetail()

        if (method == null) {
            log.error { "method is null" }
            return httpResponse
        }

        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            return httpResponse
        }

        val reqSpec = restClient.method(method)
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)

        if (body != null) {
            reqSpec.body(body)
        }

        if (StringUtils.hasText(apikey)) {
            reqSpec.header("Authorization", apikey!!)
        }

        try {
            log.debug {
                "send restapi request:\nurl: $url\nbody: ${if (body == null) "null" else jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body)}"
            }

            reqSpec.retrieve()
                .onStatus(HttpStatusCode::is1xxInformational) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    response.close()
                }
                .onStatus(HttpStatusCode::is2xxSuccessful) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    httpResponse.is2xxSuccessful = true
                    httpResponse.request = request
                    httpResponse.closableResponse = response
                }
                .onStatus(HttpStatusCode::is3xxRedirection) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    response.close()
                }
                .onStatus(HttpStatusCode::isError) { request, response ->
                    log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                    httpResponse.request = request
                    try {
                        response.use {
                            log.error { "restapi error response body: ${String(response.body.readAllBytes())}" }
                        }
                    } catch (e: IOException) {
                        log.error(e) { e.message ?: "failed to read error response body" }
                    }
                }
                .toBodilessEntity()
        } catch (e: ResourceAccessException) {
            logResourceAccessException(method, url, e)
        } catch (e: Exception) {
            log.error(e) { e.message ?: "unexpected error while sending restapi request detail" }
        }

        return httpResponse
    }

    fun sendAsync(method: HttpMethod?, url: String, apikey: String?, body: Any?, handle: ResponseHandler) {
        if (method == null) {
            log.error { "method is null" }
            try {
                handle.onReceived(false, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "response handler failed" }
            }
            return
        }

        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            try {
                handle.onReceived(false, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "response handler failed" }
            }
            return
        }

        val reqSpec = restClient.method(method)
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)

        if (body != null) {
            reqSpec.body(body)
        }

        if (StringUtils.hasText(apikey)) {
            reqSpec.header("Authorization", apikey!!)
        }

        try {
            log.debug {
                "send restapi request:\nurl: $url\nbody: ${if (body == null) "null" else jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body)}"
            }

            Thread.startVirtualThread {
                try {
                    reqSpec.retrieve()
                        .onStatus(HttpStatusCode::is1xxInformational) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            response.close()
                        }
                        .onStatus(HttpStatusCode::is2xxSuccessful) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            try {
                                response.use {
                                    val respBody = jsonMapper.readValue(response.body.readAllBytes(), JsonNode::class.java)
                                    handle.onReceived(true, respBody)
                                }
                            } catch (e: Exception) {
                                log.error(e) { e.message ?: "failed to process successful async response" }
                            }
                        }
                        .onStatus(HttpStatusCode::is3xxRedirection) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            response.close()
                        }
                        .onStatus(HttpStatusCode::isError) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            try {
                                response.use {
                                    log.error { "restapi error response body: ${String(response.body.readAllBytes())}" }
                                    handle.onReceived(false, null)
                                }
                            } catch (ex: Exception) {
                                log.error(ex) { ex.message ?: "failed to process error async response" }
                            }
                        }
                        .toBodilessEntity()
                } catch (e: ResourceAccessException) {
                    logResourceAccessException(method, url, e)
                    try {
                        handle.onReceived(false, null)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "response handler failed" }
                    }
                } catch (e: Exception) {
                    log.error(e) { e.message ?: "unexpected error while sending async restapi request" }
                    try {
                        handle.onReceived(false, null)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "response handler failed" }
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to start async restapi request" }
            try {
                handle.onReceived(false, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "response handler failed" }
            }
        }
    }

    fun sendAsyncDetail(method: HttpMethod?, url: String, apikey: String?, body: Any?, handle: ResponseDetailHandler) {
        if (method == null) {
            log.error { "method is null" }
            try {
                handle.onReceived(false, null, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "response detail handler failed" }
            }
            return
        }

        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            try {
                handle.onReceived(false, null, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "response detail handler failed" }
            }
            return
        }

        val reqSpec = restClient.method(method)
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)

        if (body != null) {
            reqSpec.body(body)
        }

        if (StringUtils.hasText(apikey)) {
            reqSpec.header("Authorization", apikey!!)
        }

        try {
            log.debug {
                "send restapi request:\nurl: $url\nbody: ${if (body == null) "null" else jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body)}"
            }

            Thread.startVirtualThread {
                try {
                    reqSpec.retrieve()
                        .onStatus(HttpStatusCode::is1xxInformational) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            response.close()
                        }
                        .onStatus(HttpStatusCode::is2xxSuccessful) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            try {
                                response.use {
                                    handle.onReceived(false, request, response)
                                }
                            } catch (ex: Exception) {
                                log.error(ex) { ex.message ?: "failed to process successful async detail response" }
                            }
                        }
                        .onStatus(HttpStatusCode::is3xxRedirection) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            response.close()
                        }
                        .onStatus(HttpStatusCode::isError) { request, response ->
                            log.info { "received restapi response: ${response.statusCode}, ${request.uri}" }
                            try {
                                response.use {
                                    log.error { "restapi error response body: ${String(response.body.readAllBytes())}" }
                                    handle.onReceived(false, request, null)
                                }
                            } catch (ex: Exception) {
                                log.error(ex) { ex.message ?: "failed to process error async detail response" }
                            }
                        }
                        .toBodilessEntity()
                } catch (e: ResourceAccessException) {
                    logResourceAccessException(method, url, e)
                    try {
                        handle.onReceived(false, null, null)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "response detail handler failed" }
                    }
                } catch (e: Exception) {
                    log.error(e) { e.message ?: "unexpected error while sending async restapi request detail" }
                    try {
                        handle.onReceived(false, null, null)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "response detail handler failed" }
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to start async restapi request detail" }
            try {
                handle.onReceived(false, null, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "response detail handler failed" }
            }
        }
    }
}
