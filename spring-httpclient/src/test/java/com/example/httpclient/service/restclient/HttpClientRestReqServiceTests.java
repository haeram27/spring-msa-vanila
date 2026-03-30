package com.example.httpclient.service.restclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpClientRestReqServiceTests {

    private HttpClient httpClient;
    private HttpClientRestReqService service;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        service = new HttpClientRestReqService(httpClient);
    }

    @Test
    void head_shouldReturnSuccessFor2xx() throws Exception {
        HttpResponse<String> mockedResponse = mockResponse(200, "");
        when(httpClient.send(any(HttpRequest.class), anyStringBodyHandler()))
            .thenReturn(mockedResponse);

        HttpClientRestReqService.HttpClientResponseDetail result =
            service.head("https://example.com/health", "token-1");

        assertNotNull(result.request);
        assertEquals("HEAD", result.request.method());
        assertEquals("https://example.com/health", result.request.uri().toString());
        assertEquals("Bearer token-1", result.request.headers().firstValue("Authorization").orElse(null));
        assertTrue(result.is2xxSuccessful);
        assertNotNull(result.response);
        assertEquals(200, result.response.statusCode());
    }

    @Test
    void get_shouldReturnFailureFor4xx() throws Exception {
        HttpResponse<String> mockedResponse = mockResponse(404, "not found");
        when(httpClient.send(any(HttpRequest.class), anyStringBodyHandler()))
            .thenReturn(mockedResponse);

        HttpClientRestReqService.HttpClientResponseDetail result =
            service.get("https://example.com/not-found", null);

        assertNotNull(result.request);
        assertEquals("GET", result.request.method());
        assertFalse(result.is2xxSuccessful);
        assertNotNull(result.response);
        assertEquals(404, result.response.statusCode());
    }

    @Test
    void post_shouldReturnSuccessFor2xx() throws Exception {
        HttpResponse<String> mockedResponse = mockResponse(201, "created");
        when(httpClient.send(any(HttpRequest.class), anyStringBodyHandler()))
            .thenReturn(mockedResponse);

        HttpClientRestReqService.HttpClientResponseDetail result =
            service.post("https://example.com/items", "token-2", "{\"name\":\"a\"}");

        assertNotNull(result.request);
        assertEquals("POST", result.request.method());
        assertTrue(result.request.bodyPublisher().isPresent());
        assertEquals("Bearer token-2", result.request.headers().firstValue("Authorization").orElse(null));
        assertTrue(result.is2xxSuccessful);
        assertNotNull(result.response);
        assertEquals(201, result.response.statusCode());
    }

    @Test
    void headAsync_shouldInvokeHandler() throws Exception {
        HttpResponse<String> mockedResponse = mockResponse(200, "");
        when(httpClient.send(any(HttpRequest.class), anyStringBodyHandler()))
            .thenReturn(mockedResponse);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(false);
        AtomicReference<HttpRequest> reqRef = new AtomicReference<>();
        AtomicReference<HttpResponse<String>> respRef = new AtomicReference<>();

        service.headAsync("https://example.com/health", "token-3", (is2xxSuccessful, request, response) -> {
            ok.set(is2xxSuccessful);
            reqRef.set(request);
            respRef.set(response);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(ok.get());
        assertNotNull(reqRef.get());
        assertEquals("HEAD", reqRef.get().method());
        assertNotNull(respRef.get());
        assertEquals(200, respRef.get().statusCode());
    }

    @Test
    void getAsync_shouldInvokeHandlerWithFailureFor4xx() throws Exception {
        HttpResponse<String> mockedResponse = mockResponse(500, "server error");
        when(httpClient.send(any(HttpRequest.class), anyStringBodyHandler()))
            .thenReturn(mockedResponse);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(true);
        AtomicReference<HttpRequest> reqRef = new AtomicReference<>();
        AtomicReference<HttpResponse<String>> respRef = new AtomicReference<>();

        service.getAsync("https://example.com/fail", null, (is2xxSuccessful, request, response) -> {
            ok.set(is2xxSuccessful);
            reqRef.set(request);
            respRef.set(response);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertFalse(ok.get());
        assertNotNull(reqRef.get());
        assertEquals("GET", reqRef.get().method());
        assertNotNull(respRef.get());
        assertEquals(500, respRef.get().statusCode());
    }

    @Test
    void postAsync_shouldInvokeHandlerWithFailureWhenIOExceptionOccurs() throws Exception {
        when(httpClient.send(any(HttpRequest.class), anyStringBodyHandler()))
            .thenThrow(new IOException("connection failed"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ok = new AtomicBoolean(true);
        AtomicReference<HttpRequest> reqRef = new AtomicReference<>();
        AtomicReference<HttpResponse<String>> respRef = new AtomicReference<>();

        service.postAsync("https://example.com/items", "token-4", "{\"k\":1}",
            (is2xxSuccessful, request, response) -> {
                ok.set(is2xxSuccessful);
                reqRef.set(request);
                respRef.set(response);
                latch.countDown();
            });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertFalse(ok.get());
        assertNotNull(reqRef.get());
        assertEquals("POST", reqRef.get().method());
        assertNull(respRef.get());
    }

    @SuppressWarnings("unchecked")
    private BodyHandler<String> anyStringBodyHandler() {
        return (BodyHandler<String>) any(HttpResponse.BodyHandler.class);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }
}
