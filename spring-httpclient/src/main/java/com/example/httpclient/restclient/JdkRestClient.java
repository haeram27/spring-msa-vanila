package com.example.httpclient.restclient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum JdkRestClient {
    INSTANCE;

    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 3; // 1~5
    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 20; // 10~30
    private HttpClient defaultHttpClient = createTrustAllHttpClient();
    private HttpClient httpClient = defaultHttpClient;

    @FunctionalInterface
    public interface ResponseHandler {
        public void onReceived(boolean isHttpSuccessful, byte[] body);
    }

    @FunctionalInterface
    public interface ResponseDetailHandler {
        void onReceived(boolean is2xxSuccessful, HttpRequest request, HttpResponse<byte[]> response);
    }

    public static class Response {
        public Response() {
            this.is2xxSuccessful = false;
            this.body = new byte[0];
        }

        boolean is2xxSuccessful;
        byte[] body;
    }

    public static class ResponseDetail {
        boolean is2xxSuccessful;
        HttpRequest request;
        HttpResponse<byte[]> response;
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
                .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS)) // connection timeout
                .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn("failed to create SSLContext that trusts all certificates, fallback to default HttpClient without custom SSLContext");
            return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS)) // connection timeout
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

    private static Throwable getRootCause(Throwable throwable) {
        var current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        var current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        return false;
    }

    private void logResourceAccessException(String url, IOException e) {
        var rootCause = getRootCause(e);
        var errorType = rootCause.getClass().getSimpleName();
        var errorMessage = hasText(rootCause.getMessage()) ? rootCause.getMessage() : "no message";

        if (hasCause(e, HttpConnectTimeoutException.class)) {
            log.error("connect timeout error: url={}, type={}, message={}", url, errorType, errorMessage, e);
            return;
        }

        // can NOT reach to server
        if (hasCause(e, ConnectException.class)) {
            log.error("connection error: url={}, type={}, message={}", url, errorType, errorMessage, e);
            return;
        }

        if (hasCause(e, HttpTimeoutException.class) || hasCause(e, SocketTimeoutException.class)) {
            log.error("timeout error: url={}, type={}, message={}", url, errorType, errorMessage, e);
            return;
        }

        if (hasCause(e, UnknownHostException.class)) {
            log.error("dns error: url={}, type={}, message={}", url, errorType, errorMessage, e);
            return;
        }

        log.error("io error: url={}, type={}, message={}", url, errorType, errorMessage, e);
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
            HttpResponse<byte[]> httpResponse = null;
            try {
                log.info("http.request url={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());
            } catch (java.io.IOException e) {
                log.error("network error:");
                logResourceAccessException(url, e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.body = httpResponse.body() == null ? new byte[0] : httpResponse.body();
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
            HttpResponse<byte[]> httpResponse = null;
            try {
                log.info("http.request url={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());
            } catch (java.io.IOException e) {
                log.error("network error:");
                logResourceAccessException(url, e);
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
            handler.onReceived(false, new byte[0]);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .HEAD()
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
                    HttpResponse<byte[]> httpResponse = null;
                    try {
                        log.info("http.request url={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                        log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, Objects.requireNonNullElse(httpResponse.body(), new byte[0]));
                        } else {
                            handler.onReceived(false, new byte[0]);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error:");
                        logResourceAccessException(url, e);
                        handler.onReceived(false, new byte[0]);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, new byte[0]);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, new byte[0]);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, new byte[0]);
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
                    HttpResponse<byte[]> httpResponse = null;
                    try {
                        log.info("http.request url={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                        log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } else {
                            handler.onReceived(false, httpRequest, httpResponse);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error:");
                        logResourceAccessException(url, e);
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
            HttpResponse<byte[]> httpResponse = null;
            try {
                log.info("http.request url={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());
            } catch (java.io.IOException e) {
                log.error("network error:");
                logResourceAccessException(url, e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.body = httpResponse.body() == null ? new byte[0] : httpResponse.body();
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
            HttpResponse<byte[]> httpResponse = null;
            try {
                log.info("http.request url={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());
            } catch (java.io.IOException e) {
                log.error("network error:");
                logResourceAccessException(url, e);
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
            handler.onReceived(false, new byte[0]);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
                    HttpResponse<byte[]> httpResponse = null;
                    try {
                        log.info("http.request url={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                        log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, Objects.requireNonNullElse(httpResponse.body(), new byte[0]));
                        } else {
                            handler.onReceived(false, new byte[0]);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error:");
                        logResourceAccessException(url, e);
                        handler.onReceived(false, new byte[0]);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, new byte[0]);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, new byte[0]);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, new byte[0]);
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
                    HttpResponse<byte[]> httpResponse = null;
                    try {
                        log.info("http.request url={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                        log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } else {
                            handler.onReceived(false, httpRequest, httpResponse);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error:");
                        logResourceAccessException(url, e);
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
            HttpResponse<byte[]> httpResponse = null;
            try {
                log.info("http.request url={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());
            } catch (java.io.IOException e) {
                log.error("network error:");
                logResourceAccessException(url, e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return response;
            }

            if (httpResponse != null) {
                response.body = httpResponse.body() == null ? new byte[0] : httpResponse.body();
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
            HttpResponse<byte[]> httpResponse = null;
            try {
                log.info("http.request url={}", httpRequest.uri());
                httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());
            } catch (java.io.IOException e) {
                log.error("network error:");
                logResourceAccessException(url, e);
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
            handler.onReceived(false, new byte[0]);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
                    HttpResponse<byte[]> httpResponse = null;
                    try {
                        log.info("http.request url={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                        log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, Objects.requireNonNullElse(httpResponse.body(), new byte[0]));
                        } else {
                            handler.onReceived(false, new byte[0]);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error:");
                        logResourceAccessException(url, e);
                        handler.onReceived(false, new byte[0]);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, new byte[0]);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, new byte[0]);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, new byte[0]);
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
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
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
                    HttpResponse<byte[]> httpResponse = null;
                    try {
                        log.info("http.request url={}", httpRequest.uri());
                        httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                        log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } else {
                            handler.onReceived(false, httpRequest, httpResponse);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error:");
                        logResourceAccessException(url, e);
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
     * @param pagenationQueryFormat  the format of the pagination query parameters, which should contain two '%d' placeholders for page number and page size (e.g., "?page=%d&size=%d"). If the provided format is invalid, a default format will be used.
     * @param totalCount    the total number of items to be paginated, used to calculate the total number of pages
     * @param pageSize  the number of items per page, used to calculate the total number of pages
     * @param timeoutSeconds the maximum time to wait for all page requests to complete, in seconds. If the timeout is reached before all requests complete, the method will return the results collected so far.
     * @return  Map<Integer, byte[]>: a map where the key is the page number and the value is the response body for that page. If a page fails to fetch, its length will be 0.
     */
    public Map<Integer, byte[]> getAllPages(String url, String apikey, String pagenationQueryFormat, int totalCount, int pageSize, int timeoutSeconds) {
        var results = new ConcurrentHashMap<Integer, byte[]>();

        var totalPages = getTotalPages(totalCount, pageSize);
        if (totalPages <= 0) {
            log.warn("No pages to fetch: totalPages={}", totalPages);
            return results;
        }
        CountDownLatch latch = new CountDownLatch(totalPages);

        String defaultPagenationFormat = "?page=%d&size=%d";
        String finalPagenationFormat;
        if (!hasText(pagenationQueryFormat)
            || !pagenationQueryFormat.contains("%d")
            || pagenationQueryFormat.indexOf("%d") == pagenationQueryFormat.lastIndexOf("%d")) {
            log.warn("Invalid pagenationQueryFormat: {}, use default body format: {}",
                pagenationQueryFormat, defaultPagenationFormat);
            finalPagenationFormat = defaultPagenationFormat;
        } else {
            finalPagenationFormat = pagenationQueryFormat;
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
            latch.await(timeoutSeconds, TimeUnit.SECONDS); // Wait for all pages to be collected or timeout after specified seconds
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
     * @param requestBodyFormat  the format of the pagination body, which should contain two '%d' placeholders for page number and page size (e.g., "{\"pageNumber\":%d,\"pageSize\":%d}"). If the provided format is invalid, a default format will be used.
     * @param totalCount    the total number of items to be paginated, used to calculate the total number of pages
     * @param pageSize  the number of items per page, used to calculate the total number of pages
     * @param timeoutSeconds the maximum time to wait for all page requests to complete, in seconds. If the timeout is reached before all requests complete, the method will return the results collected so far.
     * @return  Map<Integer, byte[]>: a map where the key is the page number and the value is the response body for that page. If a page fails to fetch, its length will be 0.
     */
    public Map<Integer, byte[]> postAllPages(String url, String apikey, String requestBodyFormat, int totalCount, int pageSize, int timeoutSeconds) {
        var results = new ConcurrentHashMap<Integer, byte[]>();

        var totalPages = getTotalPages(totalCount, pageSize);
        if (totalPages <= 0) {
            log.warn("No pages to fetch: totalPages={}", totalPages);
            return results;
        }
        CountDownLatch latch = new CountDownLatch(totalPages);

        String defaultRequestBodyFormat = "{\"pageNumber\":%d,\"pageSize\":%d}";
        String finalRequestBodyFormat;
        if (!hasText(requestBodyFormat)
            || !requestBodyFormat.contains("%d")
            || requestBodyFormat.indexOf("%d") == requestBodyFormat.lastIndexOf("%d")) {
            log.warn("Invalid requestBodyFormat: {}, use default pagenation body format: {}",
                requestBodyFormat, defaultRequestBodyFormat);
            finalRequestBodyFormat = defaultRequestBodyFormat;
        } else {
            // remove all whitespace characters to avoid formatting issues in JSON body
            finalRequestBodyFormat = requestBodyFormat.replaceAll("\\s+", "");
        }

        for (int page = 1; page <= totalPages; page++) {
            final int currentPage = page;
            String formattedBody = String.format(finalRequestBodyFormat, page, pageSize);
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
            latch.await(timeoutSeconds, TimeUnit.SECONDS); // Wait for all pages to be collected or timeout after specified seconds
        } catch (java.lang.InterruptedException e) {
            log.warn("Page collection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupt status after catching InterruptedException
        } catch (Exception e) {
            log.error("Error while waiting for page collection: {}", e.getMessage(), e);
        }

        return results;
    }
}
