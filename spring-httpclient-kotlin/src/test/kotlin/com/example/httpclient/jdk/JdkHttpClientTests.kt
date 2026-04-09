package com.example.httpclient.jdk

import com.example.EvaluatedTimeTests
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.PushPromiseHandler
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JdkHttpClientTests : EvaluatedTimeTests() {

    @Autowired
    @Qualifier("jdkHttpClient")
    private lateinit var httpClient: HttpClient

    private val urlGetLocal = "http://localhost:8181/greeting"
    private val urlGetTodoAll = "https://jsonplaceholder.typicode.com/todos"
    private val urlGetTodoOne = "https://jsonplaceholder.typicode.com/todos/1"

    @Test
    fun get() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetLocal))
            .GET()
            .build()

        val response = try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: java.io.IOException) {
            System.err.println("네트워크 오류: ${e.message}")
            throw e
        } catch (e: InterruptedException) {
            System.err.println("요청 인터럽트: ${e.message}")
            Thread.currentThread().interrupt()
            throw e
        }

        println(response.statusCode())
        println(response.body())

        if (response.statusCode() in 200..299) {
            System.err.println("HTTP Success: ${response.statusCode()}")
            System.err.println("Response Body: ${response.body()}")
        }

        if (response.statusCode() in 400..499) {
            System.err.println("HTTP Client Error: ${response.statusCode()}")
            System.err.println("Error Body: ${response.body()}")
            throw RuntimeException("HTTP ${response.statusCode()}")
        }

        if (response.statusCode() in 500..599) {
            System.err.println("HTTP Server Error: ${response.statusCode()}")
            System.err.println("Error Body: ${response.body()}")
            throw RuntimeException("HTTP ${response.statusCode()}")
        }
    }

    @Test
    fun getAsync() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoOne))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()

        val future: CompletableFuture<HttpResponse<String>> =
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())

        val response = try {
            future.get(10, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            System.err.println("응답 타임아웃 (10초 초과)")
            throw e
        } catch (e: ExecutionException) {
            System.err.println("비동기 오류: ${e.cause?.message}")
            throw e
        }

        println("메인 응답 상태: ${response.statusCode()}")
        println("메인 응답 본문: ${response.body()}")

        if (response.statusCode() in 200..299) {
            System.err.println("HTTP Success: ${response.statusCode()}")
            System.err.println("Response Body: ${response.body()}")
        }

        if (response.statusCode() in 400..499) {
            System.err.println("HTTP Client Error: ${response.statusCode()}")
            System.err.println("Error Body: ${response.body()}")
            throw RuntimeException("HTTP ${response.statusCode()}")
        }

        if (response.statusCode() in 500..599) {
            System.err.println("HTTP Server Error: ${response.statusCode()}")
            System.err.println("Error Body: ${response.body()}")
            throw RuntimeException("HTTP ${response.statusCode()}")
        }
    }

    @Test
    fun getAsyncWithPushPromise() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoAll))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()

        val pushedResponses = ConcurrentHashMap<HttpRequest, CompletableFuture<HttpResponse<String>>>()

        val pushPromiseHandler = PushPromiseHandler<String> { _, pushRequest, acceptor ->
            println("Push 요청 수신: ${pushRequest.uri()}")
            pushedResponses[pushRequest] = acceptor.apply(HttpResponse.BodyHandlers.ofString())
        }

        val future = httpClient.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString(),
            pushPromiseHandler,
        )

        val response = try {
            future.get(10, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            System.err.println("응답 타임아웃 (10초 초과)")
            throw e
        } catch (e: ExecutionException) {
            System.err.println("비동기 오류: ${e.cause?.message}")
            throw e
        }

        println("메인 응답 상태: ${response.statusCode()}")
        println("메인 응답 본문: ${response.body()}")

        for ((pushRequest, pushedFuture) in pushedResponses.entries) {
            try {
                val pushed = pushedFuture.get(5, TimeUnit.SECONDS)
                println("Push URI: ${pushRequest.uri()}")
                println("Push 상태: ${pushed.statusCode()}")
            } catch (e: Exception) {
                System.err.println("Push 응답 오류: ${pushRequest.uri()} - ${e.message}")
            }
        }
    }

    @Test
    fun build() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlGetTodoOne))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.statusCode())
        println(response.body())
    }
}
