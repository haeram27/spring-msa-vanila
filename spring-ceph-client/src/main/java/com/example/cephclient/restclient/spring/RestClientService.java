package com.example.cephclient.restclient.spring;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
    ## RestClientService

    - RestClientService is a wrapper around org.springframework.web.client.RestClient that provides additional functionalities such as error handling, response mapping, and asynchronous request handling.
    - It provides methods to send HTTP requests and handle responses in a consistent way across the application.
    - It uses JsonMapper to map JSON responses to Java objects, and it handles various exceptions that may occur during the HTTP request process, such as connection timeouts, DNS errors, etc.
    - The sendAsync method allows sending HTTP requests asynchronously using virtual threads, which can improve performance and scalability when dealing with a large number of concurrent requests.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RestClientService {
    private final RestClient restClient;

    private final JsonMapper jsonMapper;

    @FunctionalInterface
    public interface ResponseHandler {
        public void onReceived(boolean is2xxSuccessful, JsonNode body);
    }

    @FunctionalInterface
    public interface ResponseDetailHandler {
        // resonse is closable, so it should be handled in the method where it is received, not returned to the caller.
        void onReceived(boolean is2xxSuccessful, HttpRequest request, ClientHttpResponse response);
    }

    public static class Response {
        boolean is2xxSuccessful;
        JsonNode body;
    }

    public static class ResponseDetail   {
        boolean is2xxSuccessful;
        HttpRequest request;
        ClientHttpResponse closableResponse;
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

    private void logResourceAccessException(HttpMethod method, String url, ResourceAccessException e) {
        var rootCause = getRootCause(e);
        var errorType = rootCause.getClass().getSimpleName();
        var errorMessage = StringUtils.hasText(rootCause.getMessage()) ? rootCause.getMessage() : "no message";

        if (hasCause(e, HttpConnectTimeoutException.class)) {
            log.error("restapi connect timeout error: method={}, url={}, type={}, message={}", method, url, errorType, errorMessage, e);
            return;
        }

        if (hasCause(e, ConnectException.class)) {
            log.error("restapi connection error: method={}, url={}, type={}, message={}", method, url, errorType, errorMessage, e);
            return;
        }

        if (hasCause(e, HttpTimeoutException.class) || hasCause(e, SocketTimeoutException.class)) {
            log.error("restapi timeout error: method={}, url={}, type={}, message={}", method, url, errorType, errorMessage, e);
            return;
        }

        if (hasCause(e, UnknownHostException.class)) {
            log.error("restapi dns error: method={}, url={}, type={}, message={}", method, url, errorType, errorMessage, e);
            return;
        }

        log.error("restapi io error: method={}, url={}, type={}, message={}", method, url, errorType, errorMessage, e);
    }

    /**
     * This method sends an HTTP request using the RestClient and returns a Response object containing the response body and a flag indicating whether the response status code is 2xx successful.
     * The response body is parsed as a JsonNode using the JsonMapper.
     * The method also handles various exceptions that may occur during the HTTP request process and logs appropriate error messages.
     * 
     * @param method
     * @param url
     * @param apikey
     * @param body
     * @return
     */
    public Response send(HttpMethod method, String url, String apikey, Object body) {

        var httpResponse = new Response();

        if (method == null) {
            log.error("method is null");
            return httpResponse;
        }

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return httpResponse;
        }

        var reqSpec = restClient.method(method)
                        .uri(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            reqSpec.body(body);
        }

        if (StringUtils.hasText(apikey)) {
            // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
            reqSpec.header("Authorization", apikey);
        }

        try {
            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            reqSpec.retrieve()
                    .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        response.close();
                    })
                    .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        httpResponse.is2xxSuccessful = true;
                        try (response) {
                            var respBody = jsonMapper.readValue(response.getBody().readAllBytes(), JsonNode.class);
                            httpResponse.body = respBody;
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    })
                    .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        response.close();
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        try (response) {
                            if (response.getBody() != null) {
                                log.error("restapi error response body: {}", new String(response.getBody().readAllBytes()));
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpResponse;
    }

    /**
     * This method is similar to send() but it returns more detailed information about the response, including the original HttpRequest and the ClientHttpResponse.
     * The ClientHttpResponse is closable, so it should be handled in the method where it is received, not returned to the caller.
     * 
     * @param method
     * @param url
     * @param apikey
     * @param body
     * @return
     */
    public ResponseDetail sendDetail(HttpMethod method, String url, String apikey, Object body) {

        var httpResponse = new ResponseDetail();

        if (method == null) {
            log.error("method is null");
            return httpResponse;
        }

        URI uri  = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            return httpResponse;
        }

        var reqSpec = restClient.method(method)
                        .uri(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            reqSpec.body(body);
        }

        if (StringUtils.hasText(apikey)) {
            // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
            reqSpec.header("Authorization", apikey);
        }

        try {
            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            reqSpec.retrieve()
                    .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        response.close();
                    })
                    .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        httpResponse.is2xxSuccessful = true;
                        httpResponse.request = request;
                        httpResponse.closableResponse = response;
                    })
                    .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        response.close();
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                        httpResponse.request = request;
                        try (response) {
                            if (response.getBody() != null) {
                                log.error("restapi error response body: {}", new String(response.getBody().readAllBytes()));
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpResponse;
    }

    /**
     * This method sends an HTTP request asynchronously using virtual threads.
     * It takes a ResponseHandler as a parameter, which is a functional interface that defines a callback method to handle the response when it is received.
     * The response body is parsed as a JsonNode and passed to the callback method along with a flag indicating whether the response status code is 2xx successful.
     * 
     * @param method
     * @param url
     * @param apikey
     * @param body
     * @param handle
     */
    public void sendAsync(HttpMethod method, String url, String apikey, Object body, ResponseHandler handle) {

        if (method == null) {
            log.error("method is null");
            try {
                handle.onReceived(false, null);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            try {
                handle.onReceived(false, null);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        var reqSpec = restClient.method(method)
                        .uri(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            reqSpec.body(body);
        }

        if (StringUtils.hasText(apikey)) {
            // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
            reqSpec.header("Authorization", apikey);
        }

        try {
            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            Thread.startVirtualThread(() -> {
                try {
                    reqSpec.retrieve()
                        .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            response.close();
                        })
                        .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            try (response) {
                                var respBody = jsonMapper.readValue(response.getBody().readAllBytes(), JsonNode.class);
                                handle.onReceived(true, respBody);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        })
                        .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            response.close();
                        })
                        .onStatus(HttpStatusCode::isError, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            try (response) {
                                if (response.getBody() != null) {
                                    log.error("restapi error response body: {}", new String(response.getBody().readAllBytes()));
                                }
                                handle.onReceived(false, null);
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        })
                        .toBodilessEntity();
                } catch (ResourceAccessException e) {
                    logResourceAccessException(method, url, e);
                    try {
                        handle.onReceived(false, null);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        handle.onReceived(false, null);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                handle.onReceived(false, null);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * This method is similar to sendAsync() but it provides more detailed information about the response, including the original HttpRequest and the ClientHttpResponse.
     * The response body is InputStream, so it should be handled in the callback method and closed after use.
     * 
     * @param method
     * @param url
     * @param apikey
     * @param body
     * @param handle
     */
    public void sendAsyncDetail(HttpMethod method, String url, String apikey, Object body, ResponseDetailHandler handle) {

        if (method == null) {
            log.error("method is null");
            try {
                handle.onReceived(false, null, null);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        URI uri = null;
        try{
            uri = URI.create(url);
        } catch (Exception e) {
            log.error("invalid request url: {}", url, e);
            try {
                handle.onReceived(false, null, null);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        var reqSpec = restClient.method(method)
                        .uri(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            reqSpec.body(body);
        }

        if (StringUtils.hasText(apikey)) {
            // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
            reqSpec.header("Authorization", apikey);
        }

        try {
            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            Thread.startVirtualThread(() -> {
                try {
                    reqSpec.retrieve()
                        .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            response.close();
                        })
                        .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            try (response) {
                                handle.onReceived(false, request, response);
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        })
                        .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            response.close();
                        })
                        .onStatus(HttpStatusCode::isError, (request, response) -> {
                            log.info(String.format("received restapi response: %s, %s", response.getStatusCode(), request.getURI()));
                            try (response) {
                                if (response.getBody() != null) {
                                    log.error("restapi error response body: {}", new String(response.getBody().readAllBytes()));
                                }
                                handle.onReceived(false, request, null);
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        })
                        .toBodilessEntity();
                } catch (ResourceAccessException e) {
                    logResourceAccessException(method, url, e);
                    try {
                        handle.onReceived(false, null, null);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        handle.onReceived(false, null, null);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                handle.onReceived(false, null, null);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
/*
    ## Body Object
    public void postEchoTest() {

        log.info("echo()");

        // var requestBody = """
        //     {
        //         title: 'foo',
        //         body: 'bar',
        //         userId: 1
        //     }
        //     """;
        record RequestBody(
                String title,
                String body,
                Integer userId) {
        }

        restClientService.sendAsync(HttpMethod.POST,
            "http://localhost:8181/api/post/echo",
            "passw@rd",
            new RequestBody("foo", "bar", 1),
            (ok, body) -> {
                if (ok) {
                    log.info("success");
                    if (body != null) {
                        try {
                            log.info(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                } else {
                    log.info("failed");
                }
            }
        );
    }
*/

/*
    ## Body String
    public void postTest() {

        log.info("getServers()");

        sendAsync(HttpMethod.POST, "http://localhost:8181/api/v1/datas", "passw@rd", "{}", (ok, body) -> {
            if (ok) {
                log.info("success");
                var response = jsonMapper.convertValue(body, MyResponse.class);
                var data = response.getBody().getData();
                var datas = jsonMapper.convertValue(data, new TypeReference<List<MyData>>(){});

                datas.forEach(d -> log.info(d.toString()));
            } else {
                log.info("failed");
            }
        });
    }
*/

