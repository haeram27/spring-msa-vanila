package com.example.httpclient.service.restclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpClientRestReqService {
    private final HttpClient httpClient;

    @FunctionalInterface
    public interface HttpClientResponseHandler {
        public void onReceived(boolean isHttpSuccessful, HttpResponse<String> response);
    }

    @FunctionalInterface
    public interface HttpClientResponseDetailHandler {
        void onReceived(boolean is2xxSuccessful, HttpRequest request, HttpResponse<String> response);
    }

    public static class HttpClientResponseDetail {
        public HttpClientResponseDetail() {
            this.is2xxSuccessful = false;
            this.request = null;
            this.response = null;
        }   

        public HttpClientResponseDetail(
                boolean is2xxSuccessful,
                HttpRequest request,
                HttpResponse<String> response
        ) {
            this.is2xxSuccessful = is2xxSuccessful;
            this.request = request;
            this.response = response;
        }

        boolean is2xxSuccessful;
        HttpRequest request;
        HttpResponse<String> response;
    }

    /**
     * HTTP HEAD Synchronized Request
     * HTTP HEAD is a method that sends the same request as GET, but does not receive a response body.
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public HttpClientResponseDetail head(String url, String apikey) {
        HttpClientResponseDetail httpResponse = new HttpClientResponseDetail();

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return httpResponse;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .HEAD()
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json");

        if (StringUtils.hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest request = builder.build();
        httpResponse.request = request;

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> response = null;
            try {
                log.info("send http request\nurl={}", request.uri());
                response = httpClient.send(request, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", response.statusCode());
            } catch (java.io.IOException e) {
                log.error("network error: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return httpResponse;
            }

            if (response != null) {
                httpResponse.response = response;
                if (response.statusCode() >= 200 && response.statusCode() < 300) httpResponse.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpResponse;
    }

    /**
     * HTTP HEAD Asynchronized Request
     * HTTP HEAD is a method that sends the same request as GET, but does not receive a response body.
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public void headAsync(String url, String apikey, HttpClientResponseDetailHandler handler) {

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            handler.onReceived(false, null, null);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .HEAD()
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json");

        if (StringUtils.hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest request = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> response = null;
                    try {
                        log.info("send http request\nurl={}\nbody={}", request.uri());
                        response = httpClient.send(request, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", response.statusCode());

                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            handler.onReceived(true, request, response);
                        } else {
                            handler.onReceived(false, request, response);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: " + e.getMessage(), e);
                        handler.onReceived(false, request, response == null ? null : response);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, request, response == null ? null : response);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, request, response == null ? null : response);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, request, null);
        }
    }

    /**
     * HTTP GET Synchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public HttpClientResponseDetail get(String url, String apikey) {
        HttpClientResponseDetail httpResponse = new HttpClientResponseDetail();

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return httpResponse;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json");

        if (StringUtils.hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest request = builder.build();
        httpResponse.request = request;

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> response = null;
            try {
                log.info("send http request\nurl={}", request.uri());
                response = httpClient.send(request, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", response.statusCode());
                log.info("response.body(): {}", response.body());
            } catch (java.io.IOException e) {
                log.error("network error: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return httpResponse;
            }

            if (response != null) {
                httpResponse.response = response;
                if (response.statusCode() >= 200 && response.statusCode() < 300) httpResponse.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpResponse;
    }

    /**
     * HTTP GET Asynchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @return
     */
    public void getAsync(String url, String apikey, HttpClientResponseDetailHandler handler) {

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            handler.onReceived(false, null, null);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json");

        if (StringUtils.hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest request = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> response = null;
                    try {
                        log.info("send http request\nurl={}\nbody={}", request.uri());
                        response = httpClient.send(request, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", response.statusCode());
                        log.info("response.body(): {}", response.body());

                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            handler.onReceived(true, request, response);
                        } else {
                            handler.onReceived(false, request, response);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: " + e.getMessage(), e);
                        handler.onReceived(false, request, response == null ? null : response);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, request, response == null ? null : response);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, request, response == null ? null : response);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, request, null);
        }
    }

    /**
     * HTTP POST Synchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param body      request body (JSON String)
     * @return
     */
    public HttpClientResponseDetail post(String url, String apikey, String body) {
        HttpClientResponseDetail httpResponse = new HttpClientResponseDetail();

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return httpResponse;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json");

        if (StringUtils.hasText(body)) {
            builder.POST(BodyPublishers.ofString(body));
        }

        if (StringUtils.hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest request = builder.build();
        httpResponse.request = request;

        try {
            /*
                HttpClient.send() Exceptions:
                IOException         network error, connection failed, response timeout etc
                InterruptedException thread interrupted while waiting for response
            */
            HttpResponse<String> response = null;
            try {
                log.info("send http request\nurl={}\nbody={}", request.uri(), body);
                response = httpClient.send(request, BodyHandlers.ofString());
                log.info("response.statusCode(): {}", response.statusCode());
                log.info("response.body(): {}", response.body());
            } catch (java.io.IOException e) {
                log.error("network error: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                log.warn("request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return httpResponse;
            }

            if (response != null) {
                httpResponse.response = response;
                if (response.statusCode() >= 200 && response.statusCode() < 300) httpResponse.is2xxSuccessful = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpResponse;
    }

    /**
     * HTTP POST Asynchronized Request
     * @param url    request URL
     * @param apikey    BearerAPI key, Base64.getEncoder().encodeToString(apikeyString.getBytes(StandardCharsets.UTF_8)))
     * @param body      request body (JSON String)
     * @return
     */
    public void postAsync(String url, String apikey, String body, HttpClientResponseDetailHandler handler) {

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            handler.onReceived(false, null, null);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json");

        if (StringUtils.hasText(body)) {
            builder.POST(BodyPublishers.ofString(body));
        }

        if (StringUtils.hasText(apikey)) {
            builder.header("Authorization", "Bearer " + apikey);
        }

        // build final HttpRequest
        HttpRequest request = builder.build();

        try {
            Thread.startVirtualThread(()->{
                    /*
                        HttpClient.send() Exceptions:
                        IOException         network error, connection failed, response timeout etc
                        InterruptedException thread interrupted while waiting for response
                    */
                    HttpResponse<String> response = null;
                    try {
                        log.info("send http request\nurl={}\nbody={}", request.uri(), body);
                        response = httpClient.send(request, BodyHandlers.ofString());
                        log.info("response.statusCode(): {}", response.statusCode());
                        log.info("response.body(): {}", response.body());

                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            handler.onReceived(true, request, response);
                        } else {
                            handler.onReceived(false, request, response);
                        }
                    } catch (java.io.IOException e) {
                        log.error("network error: " + e.getMessage(), e);
                        handler.onReceived(false, request, response == null ? null : response);
                    } catch (InterruptedException e) {
                        log.warn("request interrupted: " + e.getMessage());

                        // set interrupt status flag again as true of this thread, because the flag removed by catch InterruptedException here
                        Thread.currentThread().interrupt();
                        handler.onReceived(false, request, response == null ? null : response);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        handler.onReceived(false, request, response == null ? null : response);
                    }
                }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            handler.onReceived(false, request, null);
        }
    }
}
