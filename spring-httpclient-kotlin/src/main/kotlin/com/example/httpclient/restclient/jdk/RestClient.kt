package com.example.httpclient.restclient.jdk

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.net.http.HttpClient
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpTimeoutException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import io.github.oshai.kotlinlogging.KotlinLogging

object RestClient {
    private val log = KotlinLogging.logger {}

    private val defaultConnectTimeoutSeconds = 3
    private val defaultReadTimeoutSeconds = 20

    private val defaultHttpClient: HttpClient = createTrustAllHttpClient()
    private var httpClient: HttpClient = defaultHttpClient
    private val requestId: AtomicInteger = AtomicInteger(0)

    enum class HttpMethod {
        DELETE,
        GET,
        HEAD,
        PATCH,
        POST,
        PUT,
    }

    fun interface ResponseHandler {
        fun onReceived(isHttpSuccessful: Boolean, body: ByteArray)
    }

    fun interface ResponseDetailHandler {
        fun onReceived(is2xxSuccessful: Boolean, request: HttpRequest?, response: HttpResponse<ByteArray>?)
    }

    class Response {
        var is2xxSuccessful: Boolean = false
        var body: ByteArray = ByteArray(0)
    }

    class ResponseDetail {
        var is2xxSuccessful: Boolean = false
        var request: HttpRequest? = null
        var response: HttpResponse<ByteArray>? = null
    }

    private fun createTrustAllHttpClient(): HttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            }
        )

        return try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(defaultConnectTimeoutSeconds.toLong()))
                .build()
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to create SSLContext" }
            log.warn {
                "failed to create SSLContext that trusts all certificates, fallback to default HttpClient without custom SSLContext"
            }

            HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(defaultConnectTimeoutSeconds.toLong()))
                .build()
        }
    }

    fun setHttpClient(httpClient: HttpClient) {
        this.httpClient = httpClient
    }

    fun resetHttpClient() {
        this.httpClient = defaultHttpClient
    }

    private fun hasText(str: String?): Boolean = !str.isNullOrBlank()

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

    private fun logResourceAccessException(url: String, e: IOException) {
        val rootCause = getRootCause(e)
        val errorType = rootCause.javaClass.simpleName
        val errorMessage = if (hasText(rootCause.message)) rootCause.message else "no message"

        if (hasCause(e, HttpConnectTimeoutException::class.java)) {
            log.error(e) { "connect timeout error: url=$url, type=$errorType, message=$errorMessage" }
            return
        }

        if (hasCause(e, ConnectException::class.java)) {
            log.error(e) { "connection error: url=$url, type=$errorType, message=$errorMessage" }
            return
        }

        if (hasCause(e, HttpTimeoutException::class.java) || hasCause(e, SocketTimeoutException::class.java)) {
            log.error(e) { "timeout error: url=$url, type=$errorType, message=$errorMessage" }
            return
        }

        if (hasCause(e, UnknownHostException::class.java)) {
            log.error(e) { "dns error: url=$url, type=$errorType, message=$errorMessage" }
            return
        }

        log.error(e) { "io error: url=$url, type=$errorType, message=$errorMessage" }
    }

    private fun buildRequest(method: HttpMethod, uri: URI, apikey: String?, body: String?): HttpRequest {
        var builder = HttpRequest.newBuilder()
        when (method) {
            HttpMethod.DELETE -> builder = builder.DELETE()
            HttpMethod.GET -> builder = builder.GET()
            HttpMethod.HEAD -> builder = builder.HEAD()
            HttpMethod.PATCH -> {
                builder = if (hasText(body)) {
                    builder.method("PATCH", BodyPublishers.ofString(body))
                } else {
                    builder.method("PATCH", BodyPublishers.noBody())
                }
            }
            HttpMethod.POST -> {
                builder = if (hasText(body)) {
                    builder.POST(BodyPublishers.ofString(body))
                } else {
                    builder.POST(BodyPublishers.noBody())
                }
            }
            HttpMethod.PUT -> {
                builder = if (hasText(body)) {
                    builder.PUT(BodyPublishers.ofString(body))
                } else {
                    builder.PUT(BodyPublishers.noBody())
                }
            }
        }

        builder = builder.uri(uri)
            .timeout(Duration.ofSeconds(defaultReadTimeoutSeconds.toLong()))
            .header("Content-Type", "application/json")

        if (hasText(apikey)) {
            builder.header("Authorization", apikey)
        }

        return builder.build()
    }

    fun send(method: HttpMethod, url: String, apikey: String?, body: String?): Response {
        val response = Response()

        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            return response
        }

        val httpRequest = buildRequest(method, uri, apikey, body)

        try {
            log.info { "http.request url=${httpRequest.uri()}" }
            val httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray())
            log.info { "http.response status=${httpResponse.statusCode()} url=${httpRequest.uri()}" }

            response.body = httpResponse.body() ?: ByteArray(0)
            if (httpResponse.statusCode() in 200..299) {
                response.is2xxSuccessful = true
            } else {
                val respBody = httpResponse.body()
                if (respBody != null && respBody.isNotEmpty()) {
                    log.warn { "received non-2xx response: body=${String(respBody)}" }
                } else {
                    log.warn { "received non-2xx response with empty body" }
                }
            }
        } catch (e: IOException) {
            log.error { "network error" }
            logResourceAccessException(url, e)
        } catch (e: InterruptedException) {
            log.warn { "request interrupted: ${e.message}" }
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            log.error(e) { e.message ?: "unexpected error while sending request" }
            return response
        }

        return response
    }

    fun sendDetail(method: HttpMethod, url: String, apikey: String?, body: String?): ResponseDetail {
        val response = ResponseDetail()

        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            return response
        }

        val httpRequest = buildRequest(method, uri, apikey, body)
        response.request = httpRequest

        try {
            log.info { "http.request url=${httpRequest.uri()}" }
            val httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray())
            log.info { "http.response status=${httpResponse.statusCode()} url=${httpRequest.uri()}" }

            response.response = httpResponse
            if (httpResponse.statusCode() in 200..299) {
                response.is2xxSuccessful = true
            } else {
                val respBody = httpResponse.body()
                if (respBody != null && respBody.isNotEmpty()) {
                    log.warn { "received non-2xx response: body=${String(respBody)}" }
                } else {
                    log.warn { "received non-2xx response with empty body" }
                }
            }
        } catch (e: IOException) {
            log.error { "network error" }
            logResourceAccessException(url, e)
        } catch (e: InterruptedException) {
            log.warn { "request interrupted: ${e.message}" }
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            log.error(e) { e.message ?: "unexpected error while sending request detail" }
            return response
        }

        return response
    }

    fun sendAsync(method: HttpMethod, url: String, apikey: String?, body: String?, handler: ResponseHandler) {
        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            handler.onReceived(false, ByteArray(0))
            return
        }

        val httpRequest = buildRequest(method, uri, apikey, body)

        try {
            Thread.startVirtualThread {
                var httpResponse: HttpResponse<ByteArray>? = null
                val id = requestId.accumulateAndGet(Int.MAX_VALUE) { current, max ->
                    if (current >= max) 0 else current + 1
                }

                try {
                    log.info { "http.request.async requestId=$id url=${httpRequest.uri()}" }
                    httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray())
                    log.info { "http.response.async requestId=$id status=${httpResponse.statusCode()}" }

                    if (httpResponse.statusCode() in 200..299) {
                        try {
                            handler.onReceived(true, httpResponse.body() ?: ByteArray(0))
                        } catch (ex: Exception) {
                            log.error(ex) { ex.message ?: "handler.onReceived failed" }
                        }
                        return@startVirtualThread
                    }

                    val respBody = httpResponse.body()
                    if (respBody != null && respBody.isNotEmpty()) {
                        log.warn { "received non-2xx response: requestId=$id body=${String(respBody)}" }
                    } else {
                        log.warn { "received non-2xx response with empty body: requestId=$id" }
                    }

                    try {
                        handler.onReceived(false, respBody ?: ByteArray(0))
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                } catch (e: IOException) {
                    log.error { "network error: requestId=$id" }
                    logResourceAccessException(url, e)

                    try {
                        handler.onReceived(false, ByteArray(0))
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                } catch (e: InterruptedException) {
                    log.warn { "request interrupted: requestId=$id message=${e.message}" }
                    Thread.currentThread().interrupt()
                    try {
                        handler.onReceived(false, ByteArray(0))
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                } catch (e: Exception) {
                    log.error(e) { "unexpected error: requestId=$id message=${e.message}" }
                    try {
                        handler.onReceived(false, ByteArray(0))
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to start async request" }
            try {
                handler.onReceived(false, ByteArray(0))
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "handler.onReceived failed" }
            }
        }
    }

    fun sendAsyncDetail(method: HttpMethod, url: String, apikey: String?, body: String?, handler: ResponseDetailHandler) {
        val uri = try {
            URI.create(url)
        } catch (e: Exception) {
            log.error(e) { "invalid request url: $url" }
            handler.onReceived(false, null, null)
            return
        }

        val httpRequest = buildRequest(method, uri, apikey, body)

        try {
            Thread.startVirtualThread {
                var httpResponse: HttpResponse<ByteArray>? = null
                val id = requestId.accumulateAndGet(Int.MAX_VALUE) { current, max ->
                    if (current >= max) 0 else current + 1
                }

                try {
                    log.info { "http.request.async requestId=$id url=${httpRequest.uri()}" }
                    httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray())
                    log.info { "http.response.async requestId=$id status=${httpResponse.statusCode()}" }

                    if (httpResponse.statusCode() in 200..299) {
                        try {
                            handler.onReceived(true, httpRequest, httpResponse)
                        } catch (ex: Exception) {
                            log.error(ex) { ex.message ?: "handler.onReceived failed" }
                        }
                        return@startVirtualThread
                    }

                    val respBody = httpResponse.body()
                    if (respBody != null && respBody.isNotEmpty()) {
                        log.warn { "received non-2xx response: requestId=$id body=${String(respBody)}" }
                    } else {
                        log.warn { "received non-2xx response with empty body: requestId=$id" }
                    }

                    try {
                        handler.onReceived(false, httpRequest, httpResponse)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                } catch (e: IOException) {
                    log.error { "network error: requestId=$id" }
                    logResourceAccessException(url, e)
                    try {
                        handler.onReceived(false, httpRequest, httpResponse)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                } catch (e: InterruptedException) {
                    log.warn { "request interrupted: requestId=$id message=${e.message}" }
                    Thread.currentThread().interrupt()
                    try {
                        handler.onReceived(false, httpRequest, httpResponse)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                } catch (e: Exception) {
                    log.error(e) { "unexpected error: requestId=$id message=${e.message}" }
                    try {
                        handler.onReceived(false, httpRequest, httpResponse)
                    } catch (ex: Exception) {
                        log.error(ex) { ex.message ?: "handler.onReceived failed" }
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e) { e.message ?: "failed to start async request detail" }
            try {
                handler.onReceived(false, httpRequest, null)
            } catch (ex: Exception) {
                log.error(ex) { ex.message ?: "handler.onReceived failed" }
            }
        }
    }

    private fun getTotalPages(totalCount: Int, pageSize: Int): Int {
        if (totalCount < 0) {
            log.error { "Invalid totalCount: $totalCount" }
            return 0
        }

        if (pageSize <= 0) {
            log.error { "Invalid pageSize: $pageSize" }
            return 0
        }

        return kotlin.math.ceil(totalCount.toDouble() / pageSize).toInt()
    }

    fun getAllPages(
        url: String,
        apikey: String?,
        pagenationQueryFormat: String?,
        totalCount: Int,
        pageSize: Int,
        timeoutSeconds: Int,
    ): Map<Int, ByteArray> {
        val results = ConcurrentHashMap<Int, ByteArray>()

        val totalPages = getTotalPages(totalCount, pageSize)
        if (totalPages <= 0) {
            log.warn { "No pages to fetch: totalPages=$totalPages" }
            return results
        }

        val latch = CountDownLatch(totalPages)
        val defaultPagenationFormat = "?page=%d&size=%d"
        val finalPagenationFormat = if (!hasText(pagenationQueryFormat)
            || !pagenationQueryFormat!!.contains("%d")
            || pagenationQueryFormat.indexOf("%d") == pagenationQueryFormat.lastIndexOf("%d")
        ) {
            log.warn {
                "Invalid pagenationQueryFormat: $pagenationQueryFormat, use default body format: $defaultPagenationFormat"
            }
            defaultPagenationFormat
        } else {
            pagenationQueryFormat
        }

        for (page in 1..totalPages) {
            val currentPage = page
            val pagedUrl = url + String.format(finalPagenationFormat, page, pageSize)
            sendAsync(HttpMethod.GET, pagedUrl, apikey, null) { ok, body ->
                if (ok) {
                    results[currentPage] = body
                } else {
                    log.warn { "Failed to fetch page: $url with page: $currentPage" }
                }
                latch.countDown()
            }
        }

        try {
            val completed = latch.await(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            if (!completed) {
                log.warn { "Timeout: ${latch.count} pages still pending" }
            }
        } catch (e: InterruptedException) {
            log.warn { "Page collection interrupted: ${e.message}" }
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            log.error(e) { "Error while waiting for page collection: ${e.message}" }
        }

        return results
    }

    fun postAllPages(
        url: String,
        apikey: String?,
        requestBodyFormat: String?,
        totalCount: Int,
        pageSize: Int,
        timeoutSeconds: Int,
    ): Map<Int, ByteArray> {
        val results = ConcurrentHashMap<Int, ByteArray>()

        val totalPages = getTotalPages(totalCount, pageSize)
        if (totalPages <= 0) {
            log.warn { "No pages to fetch: totalPages=$totalPages" }
            return results
        }

        val latch = CountDownLatch(totalPages)
        val defaultRequestBodyFormat = "{\"pageNumber\":%d,\"pageSize\":%d}"
        val finalRequestBodyFormat = if (!hasText(requestBodyFormat)
            || !requestBodyFormat!!.contains("%d")
            || requestBodyFormat.indexOf("%d") == requestBodyFormat.lastIndexOf("%d")
        ) {
            log.warn {
                "Invalid requestBodyFormat: $requestBodyFormat, use default pagenation body format: $defaultRequestBodyFormat"
            }
            defaultRequestBodyFormat
        } else {
            requestBodyFormat.replace("\\s+".toRegex(), "")
        }

        for (page in 1..totalPages) {
            val currentPage = page
            val formattedBody = String.format(finalRequestBodyFormat, page, pageSize)
            sendAsync(HttpMethod.POST, url, apikey, formattedBody) { ok, body ->
                if (ok) {
                    results[currentPage] = body
                } else {
                    log.warn { "Failed to fetch page: $url with page: $currentPage" }
                }
                latch.countDown()
            }
        }

        try {
            val completed = latch.await(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            if (!completed) {
                log.warn { "Timeout: ${latch.count} pages still pending" }
            }
        } catch (e: InterruptedException) {
            log.warn { "Page collection interrupted: ${e.message}" }
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            log.error(e) { "Error while waiting for page collection: ${e.message}" }
        }

        return results
    }
}
