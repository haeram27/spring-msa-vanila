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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
    ## RestClient

    - RestClient is a singleton enum that provides methods to send HTTP requests using Java's built-in HttpClient.
    - It supports both synchronous and asynchronous HTTP requests, and it allows for custom error handling and response processing through functional interfaces.
    - The send method sends a synchronous HTTP request and returns a Response object containing the success status and response body.
    - The sendDetail method sends a synchronous HTTP request and returns a ResponseDetail object containing the success status, the original HttpRequest, and the HttpResponse.
    - The sendAsync method sends an asynchronous HTTP request and uses a ResponseHandler functional interface to process the response when it is received.
    - The sendAsyncDetail method sends an asynchronous HTTP request and uses a ResponseDetailHandler functional interface to process the response details when it is received.
    - The class also includes utility methods for creating an HttpClient that trusts all SSL certificates, logging resource access exceptions, and calculating total pages for pagination.
 */

@Slf4j
public enum RestClient {
    INSTANCE;

    private final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 3; // 1~5
    private final int DEFAULT_READ_TIMEOUT_SECONDS = 20; // 10~30

    // Virtual thread executor used for async send submission and handler execution
    private final ExecutorService executor = new ThreadPoolExecutor(
        0, 100,
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        Thread.ofVirtual().name("rest-client-").factory()
    );

    private HttpClient defaultHttpClient = createTrustAllHttpClient();
    private HttpClient httpClient = defaultHttpClient;

    public enum HttpMethod {
        // https://datatracker.ietf.org/doc/html/rfc7231#section-4
        DELETE, GET, HEAD, PATCH, POST, PUT
    }

    @FunctionalInterface
    public interface ResponseHandler {
        public void onReceived(boolean isHttpSuccessful, byte[] body);
    }

    @FunctionalInterface
    public interface ResponseDetailHandler {
        void onReceived(boolean is2xxSuccessful, HttpRequest request, HttpResponse<byte[]> response);
    }

    public class Response {
        public Response() {
            this.is2xxSuccessful = false;
            this.body = new byte[0];
        }

        public boolean is2xxSuccessful;
        public byte[] body;
    }

    public class ResponseDetail {
        public boolean is2xxSuccessful;
        public HttpRequest request;
        public HttpResponse<byte[]> response;
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
                .executor(executor)
                .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS)) // connection timeout
                .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn("failed to create SSLContext that trusts all certificates, fallback to default HttpClient without custom SSLContext");
            return HttpClient.newBuilder()
                .executor(executor)
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

    /**
     * Shutdown resources created by RestClient (executor).
     * Does not close HttpClient supplied externally via {@link #setHttpClient}.
     * This executor use virtual threads, which will not keep the application running if all other non-daemon threads have completed.
     * However, it is still recommended to shutdown the executor when it is no longer needed to free up resources and allow for proper cleanup of any pending tasks.
     */
    public void shutdownExecutor() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
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
     * HTTP POST Synchronized Request
     * 
     * @param url    request URL
     * @param apikey    api authentication key like "Bearer <token>", "Bearer " + Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8))), can be null or empty if the request does not require authentication
     * @param body      request body (JSON String), can be null or empty if the request does not have body, such as GET, DELETE, HEAD, etc.
     * @return is2xxSuccessful is true if the response status code is 2xx
     *         body will not be null, but empty byte array if the response does not have body or the request failed due to network error, timeout, invalid URL, etc.
     */
    public Response send(HttpMethod method, String url, String apikey, String body) {
        var response = new Response();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        switch (method) {
            case DELETE:
                builder = builder.DELETE();
                break;
            case GET:
                builder = builder.GET();
                break;
            case HEAD:
                builder = builder.HEAD();
                break;
            case PATCH:
            if (hasText(body)) {
                    builder = builder.method("PATCH", BodyPublishers.ofString(body));
                } else {
                    builder = builder.method("PATCH", BodyPublishers.noBody());
                }
                break;
            case POST:
                if (hasText(body)) {
                    builder = builder.POST(BodyPublishers.ofString(body));
                } else {
                    builder = builder.POST(BodyPublishers.noBody());
                }
                break;
            case PUT:
                if (hasText(body)) {
                    builder = builder.PUT(BodyPublishers.ofString(body));
                } else {
                    builder = builder.PUT(BodyPublishers.noBody());
                }
                break;
            default:
                log.warn("default to GET method for unsupported http method: {}, url={}", method, url);
                break;
        }

        builder = builder.uri(uri)
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            try {
                log.info("http.request url={}", httpRequest.uri());
                var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                response.body = httpResponse.body() == null ? new byte[0] : httpResponse.body();
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                    response.is2xxSuccessful = true;
                } else {
                    log.warn("received non-2xx response: body={}", (httpResponse.body().length > 0) ? new String(httpResponse.body()) : "");
                }
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP POST Synchronized Request
     * 
     * @param url    request URL
     * @param apikey    api authentication key like "Bearer <token>", "Bearer " + Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8))), can be null or empty if the request does not require authentication
     * @param body      request body (JSON String), can be null or empty if the request does not have body, such as GET, DELETE, HEAD, etc.
     * @return
     */
    public ResponseDetail sendDetail(HttpMethod method, String url, String apikey, String body) {
        ResponseDetail response = new ResponseDetail();

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return response;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        switch (method) {
            case DELETE:
                builder = builder.DELETE();
                break;
            case GET:
                builder = builder.GET();
                break;
            case HEAD:
                builder = builder.HEAD();
                break;
            case PATCH:
            if (hasText(body)) {
                    builder = builder.method("PATCH", BodyPublishers.ofString(body));
                } else {
                    builder = builder.method("PATCH", BodyPublishers.noBody());
                }
                break;
            case POST:
                if (hasText(body)) {
                    builder = builder.POST(BodyPublishers.ofString(body));
                } else {
                    builder = builder.POST(BodyPublishers.noBody());
                }
                break;
            case PUT:
                if (hasText(body)) {
                    builder = builder.PUT(BodyPublishers.ofString(body));
                } else {
                    builder = builder.PUT(BodyPublishers.noBody());
                }
                break;
            default:
                log.warn("default to GET method for unsupported http method: {}, url={}", method, url);
                break;
        }

        builder = builder.uri(uri)
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", apikey);
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
            try {
                log.info("http.request url={}", httpRequest.uri());
                var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                log.info("http.response status={} url={}", httpResponse.statusCode(), httpRequest.uri());

                response.response = httpResponse;
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                    response.is2xxSuccessful = true;
                } else {
                    log.warn("received non-2xx response: body={}", (httpResponse.body().length > 0) ? new String(httpResponse.body()) : "");
                }
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * HTTP POST Asynchronized Request
     * 
     * @param url    request URL
     * @param apikey    api authentication key like "Bearer <token>", "Bearer " + Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8))), can be null or empty if the request does not require authentication
     * @param body      request body (JSON String), can be null or empty if the request does not have body, such as GET, DELETE, HEAD, etc.
     * @param handler   response handler to handle the response details such as status code, headers, body, etc.
     * @return is2xxSuccessful is true if the response status code is 2xx
     *         body will not be null, but empty byte array if the response does not have body or the request failed due to network error, timeout, invalid URL, etc.
     */
    public void sendAsync(HttpMethod method, String url, String apikey, String body, ResponseHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, new byte[0]);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        switch (method) {
            case DELETE:
                builder = builder.DELETE();
                break;
            case GET:
                builder = builder.GET();
                break;
            case HEAD:
                builder = builder.HEAD();
                break;
            case PATCH:
            if (hasText(body)) {
                    builder = builder.method("PATCH", BodyPublishers.ofString(body));
                } else {
                    builder = builder.method("PATCH", BodyPublishers.noBody());
                }
                break;
            case POST:
                if (hasText(body)) {
                    builder = builder.POST(BodyPublishers.ofString(body));
                } else {
                    builder = builder.POST(BodyPublishers.noBody());
                }
                break;
            case PUT:
                if (hasText(body)) {
                    builder = builder.PUT(BodyPublishers.ofString(body));
                } else {
                    builder = builder.PUT(BodyPublishers.noBody());
                }
                break;
            default:
                log.warn("default to GET method for unsupported http method: {}, url={}", method, url);
                break;
        }

        builder = builder.uri(uri)
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            log.info("http.request url={}", httpRequest.uri());
            httpClient.sendAsync(httpRequest, BodyHandlers.ofByteArray())
                .whenCompleteAsync((httpResponse, throwable) -> {
                    if (throwable != null) {
                        Throwable t = (throwable instanceof java.util.concurrent.CompletionException)
                            ? throwable.getCause() : throwable;

                        if (t instanceof IOException) {
                            log.error("network error:");
                            logResourceAccessException(url, (IOException) t);
                        } else {
                            log.error("request failed: {}", t.getMessage(), t);
                        }

                        try {
                            handler.onReceived(false, new byte[0]);
                        } catch (Exception e) {
                            log.error("handler threw: {}", e.getMessage(), e);
                        }
                        return;
                    }

                    if (Objects.isNull(httpResponse)) {
                        try {
                            handler.onReceived(false, new byte[0]);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e); }
                        return;
                    }

                    byte[] bodyBytes = Objects.isNull(httpResponse.body()) ? new byte[0] : httpResponse.body();
                    if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                        try {
                            handler.onReceived(true, bodyBytes);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e); }
                    } else {
                        log.warn("received non-2xx response: body={}", (bodyBytes.length > 0) ? new String(bodyBytes) : "");
                        try {
                            handler.onReceived(false, bodyBytes);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e); }
                    }
                }, executor);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                handler.onReceived(false, new byte[0]);
            } catch (Exception ex) { log.error("handler threw: {}", ex.getMessage(), ex); }
        }
    }

    /**
     * HTTP POST Asynchronized Request
     * 
     * @param url    request URL
     * @param apikey    api authentication key like "Bearer <token>", "Bearer " + Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8))), can be null or empty if the request does not require authentication
     * @param body      request body (JSON String), can be null or empty if the request does not have body, such as GET, DELETE, HEAD, etc.
     * @param handler   response handler to handle the response details such as status code, headers, body, etc.
     * @return
     */
    public void sendAsyncDetail(HttpMethod method, String url, String apikey, String body, ResponseDetailHandler handler) {

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            handler.onReceived(false, null, null);
            return;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        switch (method) {
            case DELETE:
                builder = builder.DELETE();
                break;
            case GET:
                builder = builder.GET();
                break;
            case HEAD:
                builder = builder.HEAD();
                break;
            case PATCH:
            if (hasText(body)) {
                    builder = builder.method("PATCH", BodyPublishers.ofString(body));
                } else {
                    builder = builder.method("PATCH", BodyPublishers.noBody());
                }
                break;
            case POST:
                if (hasText(body)) {
                    builder = builder.POST(BodyPublishers.ofString(body));
                } else {
                    builder = builder.POST(BodyPublishers.noBody());
                }
                break;
            case PUT:
                if (hasText(body)) {
                    builder = builder.PUT(BodyPublishers.ofString(body));
                } else {
                    builder = builder.PUT(BodyPublishers.noBody());
                }
                break;
            default:
                log.warn("default to GET method for unsupported http method: {}, url={}", method, url);
                break;
        }

        builder = builder.uri(uri)
            .timeout(Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS))
            .header("Content-Type", "application/json");

        if (hasText(apikey)) {
            builder.header("Authorization", apikey);
        }

        // build final HttpRequest
        HttpRequest httpRequest = builder.build();

        try {
            log.info("http.request url={}", httpRequest.uri());
            httpClient.sendAsync(httpRequest, BodyHandlers.ofByteArray())
                .whenCompleteAsync((httpResponse, throwable) -> {
                    if (throwable != null) {
                        Throwable t = (throwable instanceof java.util.concurrent.CompletionException)
                            ? throwable.getCause() : throwable;

                        if (t instanceof IOException) {
                            log.error("network error:");
                            logResourceAccessException(url, (IOException) t);
                        } else {
                            log.error("request failed: {}", t.getMessage(), t);
                        }

                        try {
                            handler.onReceived(false, httpRequest, null);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e); }
                        return;
                    }

                    if (Objects.isNull(httpResponse)) {
                        try {
                            handler.onReceived(false, httpRequest, null);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e);}
                        return;
                    }

                    byte[] bodyBytes = Objects.isNull(httpResponse.body()) ? new byte[0] : httpResponse.body();
                    if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                        try {
                            handler.onReceived(true, httpRequest, httpResponse);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e); }
                    } else {
                        log.warn("received non-2xx response: body={}", (bodyBytes.length > 0) ? new String(bodyBytes) : "");
                        try {
                            handler.onReceived(false, httpRequest, httpResponse);
                        } catch (Exception e) { log.error("handler threw: {}", e.getMessage(), e); }
                    }
                }, executor);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                handler.onReceived(false, httpRequest, null);
            } catch (Exception ex) { log.error("handler threw: {}", ex.getMessage(), ex); }
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
     * Collects all pages of data from a paginated API endpoint using HTTP GET requests.
     * The method calculates the total number of pages based on the provided total count and page size,
     * then asynchronously sends GET requests for each page using the specified pagination body format.
     * The results are collected in a thread-safe map, and the method waits for all requests to complete or times out with given timeoutSeconds.
     * 
     * @param url   request URL
     * @param apikey    api authentication key like "Bearer <token>", "Bearer " + Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8))), can be null or empty if the request does not require authentication
     * @param pagenationQueryFormat  the format of the pagination query parameters, which should contain two '%d' placeholders for page number and page size (e.g., "?page=%d&size=%d"). If the provided format is invalid, a default format will be used.
     * @param totalCount    the total number of items to be paginated, used to calculate the total number of pages
     * @param pageSize  the number of items per page, used to calculate the total number of pages
     * @param timeoutSeconds the maximum time to wait for all page requests to complete, in seconds. 20~60 sec is recommended. If the timeout is reached before all requests complete, the method will return the results collected so far.
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
            sendAsync(HttpMethod.GET, pagedUrl, apikey, null, (ok, body) -> {
                if (ok) {
                    results.put(currentPage, body);
                } else {
                    log.warn("Failed to fetch page: {} with page: {}", url, currentPage);
                }
                latch.countDown();
            });
        }

        try {
            // Wait for all pages to be collected or timeout after specified seconds
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Timeout: {} pages still pending", latch.getCount());
            }
        } catch (java.lang.InterruptedException e) {
            log.warn("Page collection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupt status after catching InterruptedException
        } catch (Exception e) {
            log.error("Error while waiting for page collection: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * Collects all pages of data from a paginated API endpoint using HTTP POST requests.
     * The method calculates the total number of pages based on the provided total count and page size,
     * then asynchronously sends POST requests for each page using the specified pagination body format.
     * The results are collected in a thread-safe map, and the method waits for all requests to complete or times out with given timeoutSeconds.
     * 
     * @param url   request URL
     * @param apikey    api authentication key like "Bearer <token>", "Bearer " + Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8))), can be null or empty if the request does not require authentication
     * @param requestBodyFormat  the format of the pagination body, which should contain two '%d' placeholders for page number and page size (e.g., "{\"pageNumber\":%d,\"pageSize\":%d}"). If the provided format is invalid, a default format will be used.
     * @param totalCount    the total number of items to be paginated, used to calculate the total number of pages
     * @param pageSize  the number of items per page, used to calculate the total number of pages
     * @param timeoutSeconds the maximum time to wait for all page requests to complete, in seconds. 20~60 sec is recommended. If the timeout is reached before all requests complete, the method will return the results collected so far.
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
            sendAsync(HttpMethod.POST, url, apikey, formattedBody, (ok, body) -> {
                if (ok) {
                    results.put(currentPage, body);
                } else {
                    log.warn("Failed to fetch page: {} with page: {}", url, currentPage);
                }
                latch.countDown();
            });
        }

        try {
            // Wait for all pages to be collected or timeout after specified seconds
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Timeout: {} pages still pending", latch.getCount());
            }
        } catch (java.lang.InterruptedException e) {
            log.warn("Page collection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupt status after catching InterruptedException
        } catch (Exception e) {
            log.error("Error while waiting for page collection: {}", e.getMessage(), e);
        }

        return results;
    }
}