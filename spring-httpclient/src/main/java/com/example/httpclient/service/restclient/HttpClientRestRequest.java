package com.example.httpclient.service.restclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum HttpClientRestRequest {
    INSTANCE;

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private HttpClient defaultHttpClient = createTrustAllHttpClient();
    private HttpClient httpClient = defaultHttpClient;

    @FunctionalInterface
    public interface ResponseHandler {
        public void onReceived(boolean isHttpSuccessful, String body);
    }

    @FunctionalInterface
    public interface ResponseDetailHandler {
        void onReceived(boolean is2xxSuccessful, HttpRequest request, HttpResponse<String> response);
    }

    public static class Response {
        public Response() {
            this.is2xxSuccessful = false;
            this.body = "";
        }

        boolean is2xxSuccessful;
        String body;
    }

    public static class ResponseDetail {
        boolean is2xxSuccessful;
        HttpRequest request;
        HttpResponse<String> response;
    }

    private HttpClient createTrustAllHttpClient() {

        // Create TrustManager trusts ALL server certs
        var trustAllCerts = new javax.net.ssl.TrustManager[]{
            new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };

        try {
            // Create TrustAll SSLContext
            var sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(2)) // connection timeout
                .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn("failed to create SSLContext that trusts all certificates, fallback to default HttpClient without custom SSLContext");
            return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) // connection timeout
                .build();
        }
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void resetHttpClient() {
        this.httpClient = defaultHttpClient;
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * HTTP HEAD Synchronized Request
     * HTTP HEAD is a method that sends the same request as GET, but does not receive a response body.
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public Response head(String url, String apikey) {
        var response = new Response();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .HEAD()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> httpResponse = null;
            try {
                log.info("send http request\\nurl={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", httpResponse.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: {}", e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.body = httpResponse.body() == null ? "" : httpResponse.body();
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) response.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP HEAD Synchronized Request
     * HTTP HEAD is a method that sends the same request as GET, but does not receive a response body.
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public ResponseDetail headDetail(String url, String apikey) {
        ResponseDetail response = new ResponseDetail();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .HEAD()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();
        response.request = httpRequest;

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> httpResponse = null;
            try {
                log.info("send http request\nurl={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", httpResponse.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: {}", e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.response = httpResponse;
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) response.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP HEAD Asynchronized Request
     * HTTP HEAD is a method that sends the same request as GET, but does not receive a response body.
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public void headAsync(String url, String apikey, ResponseHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, "");
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .HEAD()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> httpResponse = null;
                    try {
                        log.info("send http request\\nurl={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", httpResponse.statusCode());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, Objects.requireNonNullElse(httpResponse.body(), ""));
                        } else {
                            handler.onReceived(false, "");
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: {}", e.getMessage(), e);
                        handler.onReceived(false, "");
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, "");
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, "");
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, "");
        }
    }

    /**
     * HTTP HEAD Asynchronized Request
     * HTTP HEAD is a method that sends the same request as GET, but does not receive a response body.
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public void headAsyncDetail(String url, String apikey, ResponseDetailHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, null, null);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .HEAD()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> httpResponse = null;
                    try {
                        log.info("send http request\\nurl={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", httpResponse.statusCode());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } else {
                            handler.onReceived(false, httpRequest, httpResponse);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: {}", e.getMessage(), e);
                        handler.onReceived(false, httpRequest, httpResponse == null ? null : httpResponse);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, httpRequest, httpResponse == null ? null : httpResponse);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, httpRequest, httpResponse == null ? null : httpResponse);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, httpRequest, null);
        }
    }

    /**
     * HTTP GET Synchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public Response get(String url, String apikey) {
        var response = new Response();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> httpResponse = null;
            try {
                log.info("send http request\\nurl={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", httpResponse.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: {}", e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.body = httpResponse.body() == null ? "" : httpResponse.body();
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) response.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP GET Synchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public ResponseDetail getDetail(String url, String apikey) {
        ResponseDetail response = new ResponseDetail();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();
        response.request = httpRequest;

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> httpResponse = null;
            try {
                log.info("send http request\\nurl={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", httpResponse.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: {}", e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.response = httpResponse;
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) response.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP GET Asynchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public void getAsync(String url, String apikey, ResponseHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, "");
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> httpResponse = null;
                    try {
                        log.info("send http request\nurl={}\nbody={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", httpResponse.statusCode());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, Objects.requireNonNullElse(httpResponse.body(), ""));
                        } else {
                            handler.onReceived(false, "");
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: {}", e.getMessage(), e);
                        handler.onReceived(false, "");
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, "");
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, "");
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, "");
        }
    }

    /**
     * HTTP GET Asynchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public void getAsyncDetail(String url, String apikey, ResponseDetailHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, null, null);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> httpResponse = null;
                    try {
                        log.info("send http request\nurl={}\nbody={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", httpResponse.statusCode());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } else {
                            handler.onReceived(false, httpRequest, httpResponse);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: {}", e.getMessage(), e);
                        handler.onReceived(false, httpRequest, httpResponse);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, httpRequest, httpResponse);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, httpRequest, httpResponse);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, httpRequest, null);
        }
    }

    /**
     * HTTP POST Synchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param body      request body (JSON String)
     * @return
     */
    public Response post(String url, String apikey, String body) {
        var response = new Response();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(body)) {
            builder.POST(BodyPublishers.ofString(body));
        }

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> httpResponse = null;
            try {
                log.info("send http request\\nurl={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", httpResponse.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: {}", e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.body = httpResponse.body() == null ? "" : httpResponse.body();
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) response.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP POST Synchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param body      request body (JSON String)
     * @return
     */
    public ResponseDetail postDetail(String url, String apikey, String body) {
        ResponseDetail response = new ResponseDetail();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(body)) {
            builder.POST(BodyPublishers.ofString(body));
        }

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();
        response.request = httpRequest;

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> httpResponse = null;
            try {
                log.info("send http request\\nurl={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", httpResponse.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: {}", e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.response = httpResponse;
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) response.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP POST Asynchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param body      request body (JSON String)
     * @param handler   response handler to handle the response details such as status code, headers, body, etc.
     * @return
     */
    public void postAsync(String url, String apikey, String body, ResponseHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, "");
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(body)) {
            builder.POST(BodyPublishers.ofString(body));
        }

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> httpResponse = null;
                    try {
                        log.info("send http request\\nurl={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", httpResponse.statusCode());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, Objects.requireNonNullElse(httpResponse.body(), ""));
                        } else {
                            handler.onReceived(false, "");
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: {}", e.getMessage(), e);
                        handler.onReceived(false, "");
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, "");
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, "");
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, "");
        }
    }

    /**
     * HTTP POST Asynchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param body      request body (JSON String)
     * @param handler   response handler to handle the response details such as status code, headers, body, etc.
     * @return
     */
    public void postAsyncDetail(String url, String apikey, String body, ResponseDetailHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, null, null);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(body)) {
            builder.POST(BodyPublishers.ofString(body));
        }

        if (hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> httpResponse = null;
                    try {
                        log.info("send http request\\nurl={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", httpResponse.statusCode());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } else {
                            handler.onReceived(false, httpRequest, httpResponse);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: {}", e.getMessage(), e);
                        handler.onReceived(false, httpRequest, httpResponse);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, httpRequest, httpResponse);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, httpRequest, httpResponse);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, httpRequest, null);
        }
    }

    private int getTotalPages(int totalCount, int pageSize) {
        if (totalCount < 0) {
            log.error("Invalid totalCount: {}", totalCount);
            return 0;
        }

        if (pageSize <= 0) {
            log.error("Invalid pageSize: {}", pageSize);
            return 0;
        }

        // Math.ceil is used to round up the total pages, ensuring that any remaining items are accounted for in an additional page
        // example: if totalCount is 10 and pageSize is 3, totalPages will be 4 (10/3 = 3.33, rounded up to 4)
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * Collects all pages of data from a paginated API endpoint using HTTP POST requests. The method calculates the total number of pages based on the provided total count and page size, then asynchronously sends POST requests for each page using the specified pagination body format. The results are collected in a thread-safe map, and the method waits for all requests to complete or times out after 3 minutes.
     * @param url   request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param pagenationFormat  the format of the pagination query parameters, which should contain two '%d' placeholders for page number and page size (e.g., "?page=%d&size=%d"). If the provided format is invalid, a default format will be used.
     * @param totalCount    the total number of items to be paginated, used to calculate the total number of pages
     * @param pageSize  the number of items per page, used to calculate the total number of pages
     * @return  Map<Integer, String>: a map where the key is the page number and the value is the response body for that page. If a page fails to fetch, its value will be null.
     */
    public Map<Integer, String> getAllPages(String url, String apikey, String pagenationFormat, int totalCount, int pageSize) {
        var results = new ConcurrentHashMap<Integer, String>();

        var totalPages = getTotalPages(totalCount, pageSize);
        if (totalPages <= 0) {
            log.warn("No pages to fetch: totalPages={}", totalPages);
            return results;
        }
        CountDownLatch latch = new CountDownLatch(totalPages);

        String defaultPagenationFormat = "?page=%d&size=%d";
        String finalPagenationFormat;
        if (!hasText(pagenationFormat)
            || !pagenationFormat.contains("%d")
            || pagenationFormat.indexOf("%d") == pagenationFormat.lastIndexOf("%d")) {
            log.warn("Invalid pagenationFormat: {}, use default body format: {}",
                pagenationFormat, defaultPagenationFormat);
            finalPagenationFormat = defaultPagenationFormat;
        } else {
            finalPagenationFormat = pagenationFormat;
        }

        for (int page = 1; page <= totalPages; page++) {
            final int currentPage = page;
            String pagedUrl = url + String.format(finalPagenationFormat, page, pageSize);
            getAsync(pagedUrl, apikey, (ok, body) -> {
                if (ok) {
                    results.put(currentPage, body);
                } else {
                    results.put(currentPage, null);
                    log.warn("Failed to fetch page: {}", pagedUrl);
                }
                latch.countDown();
            });
        }

        try {
            latch.await(3, TimeUnit.MINUTES); // Wait for all pages to be collected or timeout after 3 minutes
        } catch (java.lang.InterruptedException e) {
            log.warn("Page collection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupt status after catching InterruptedException
        } catch (Exception e) {
            log.error("Error while waiting for page collection: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * Collects all pages of data from a paginated API endpoint using HTTP POST requests. The method calculates the total number of pages based on the provided total count and page size, then asynchronously sends POST requests for each page using the specified pagination body format. The results are collected in a thread-safe map, and the method waits for all requests to complete or times out after 3 minutes.
     * @param url   request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param pagenationBodyFormat  the format of the pagination body, which should contain two '%d' placeholders for page number and page size (e.g., "{\"pageNumber\":%d,\"pageSize\":%d}"). If the provided format is invalid, a default format will be used.
     * @param totalCount    the total number of items to be paginated, used to calculate the total number of pages
     * @param pageSize  the number of items per page, used to calculate the total number of pages
     * @return  Map<Integer, String>: a map where the key is the page number and the value is the response body for that page. If a page fails to fetch, its value will be null.
     */
    public Map<Integer, String> postAllPages(String url, String apikey, String pagenationBodyFormat, int totalCount, int pageSize) {
        var results = new ConcurrentHashMap<Integer, String>();

        var totalPages = getTotalPages(totalCount, pageSize);
        if (totalPages <= 0) {
            log.warn("No pages to fetch: totalPages={}", totalPages);
            return results;
        }
        CountDownLatch latch = new CountDownLatch(totalPages);

        String defaultPagenationBodyFormat = "{\"pageNumber\":%d,\"pageSize\":%d}";
        String finalPagenationBodyFormat;
        if (!hasText(pagenationBodyFormat)
            || !pagenationBodyFormat.contains("%d")
            || pagenationBodyFormat.indexOf("%d") == pagenationBodyFormat.lastIndexOf("%d")) {
            log.warn("Invalid pagenationBodyFormat: {}, use default pagenation body format: {}",
                pagenationBodyFormat, defaultPagenationBodyFormat);
            finalPagenationBodyFormat = defaultPagenationBodyFormat;
        } else {
            // remove all whitespace characters to avoid formatting issues in JSON body
            finalPagenationBodyFormat = pagenationBodyFormat.replaceAll("\\s+", "");
        }

        for (int page = 1; page <= totalPages; page++) {
            final int currentPage = page;

            String formattedBody = String.format(finalPagenationBodyFormat, page, pageSize);
            postAsync(url, apikey, formattedBody, (ok, body) -> {
                if (ok) {
                    results.put(currentPage, body);
                } else {
                    results.put(currentPage, null);
                    log.warn("Failed to fetch page: {} with page: {}", url, formattedBody);
                }
                latch.countDown();
            });
        }

        try {
            latch.await(3, TimeUnit.MINUTES); // Wait for all pages to be collected or timeout after 3 minutes
        } catch (java.lang.InterruptedException e) {
            log.warn("Page collection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupt status after catching InterruptedException
        } catch (Exception e) {
            log.error("Error while waiting for page collection: {}", e.getMessage(), e);
        }

        return results;
    }
}
