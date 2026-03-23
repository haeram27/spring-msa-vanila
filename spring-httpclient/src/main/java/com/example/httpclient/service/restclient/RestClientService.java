package com.example.httpclient.service.restclient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestClientService {
    private final RestClient restClient;

    private final JsonMapper jsonMapper;

    @FunctionalInterface
    public interface RestClientResponseHandler {
        public void onReceived(boolean isHttpSuccessful, JsonNode body);
    }

    @FunctionalInterface
    public interface RestClientResponseDetailHandler {
        void onReceived(boolean is2xxSuccessful, HttpRequest request, ClientHttpResponse response);
    }

    public static class RestClientResponseDetail {
        boolean is2xxSuccessful;
        HttpRequest request;
        ClientHttpResponse response;
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

    public RestClientResponseDetail send(HttpMethod method, String url, String apikey, Object body) {

        var httpResponse = new RestClientResponseDetail();

        if (method == null) {
            log.error("method is null");
            return httpResponse;
        }

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return httpResponse;
        }

        try {
            var reqSpec = restClient.method(method)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                reqSpec.body(body);
            }

            if (StringUtils.hasText(apikey)) {
                // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
                reqSpec.header("Authorization", apikey);
            }

            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            reqSpec.retrieve()
                    .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                    })
                    .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        httpResponse.is2xxSuccessful = true;
                        httpResponse.request = request;
                        httpResponse.response = response;
                    })
                    .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        httpResponse.is2xxSuccessful = false;
                        httpResponse.request = request;
                        httpResponse.response = response;
                    })
                    .toBodilessEntity();

        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpResponse;
    }

    public void send(HttpMethod method, String url, String apikey, Object body, RestClientResponseHandler handle) {

        if (method == null) {
            log.error("method is null");
            return;
        }

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return;
        }

        try {
            var reqSpec = restClient.method(method)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                reqSpec.body(body);
            }

            if (StringUtils.hasText(apikey)) {
                // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
                reqSpec.header("Authorization", apikey);
            }

            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            reqSpec.retrieve()
                    .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                    })
                    .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        var respBody = jsonMapper.readValue(response.getBody().readAllBytes(), JsonNode.class);
                        handle.onReceived(true, respBody);
                    })
                    .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        handle.onReceived(false, null);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
            handle.onReceived(false, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void send(HttpMethod method, String url, String apikey, Object body,
            RestClientResponseDetailHandler handle) {

        if (method == null) {
            log.error("method is null");
            return;
        }

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return;
        }

        try {
            var reqSpec = restClient.method(method)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                reqSpec.body(body);
            }

            if (StringUtils.hasText(apikey)) {
                // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
                reqSpec.header("Authorization", apikey);
            }

            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            reqSpec.retrieve()
                    .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                    })
                    .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        handle.onReceived(true, request, response);
                    })
                    .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        handle.onReceived(false, request, response);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
            handle.onReceived(false, null, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendAsync(HttpMethod method, String url, String apikey, Object body, RestClientResponseHandler handle) {

        if (method == null) {
            log.error("method is null");
            return;
        }

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return;
        }

        try {
            var reqSpec = restClient.method(method)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                reqSpec.body(body);
            }

            if (StringUtils.hasText(apikey)) {
                // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
                reqSpec.header("Authorization", apikey);
            }

            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            new Thread(() -> {
                try {
                    reqSpec.retrieve()
                        .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        })
                        .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                            var respBody = jsonMapper.readValue(response.getBody().readAllBytes(), JsonNode.class);
                            handle.onReceived(true, respBody);
                        })
                        .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        })
                        .onStatus(HttpStatusCode::isError, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                            handle.onReceived(false, null);
                        })
                        .toBodilessEntity();
                } catch (ResourceAccessException e) {
                    logResourceAccessException(method, url, e);
                    handle.onReceived(false, null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }).start();
        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
            handle.onReceived(false, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendAsync(HttpMethod method, String url, String apikey, Object body,
            RestClientResponseDetailHandler handle) {

        if (method == null) {
            log.error("method is null");
            return;
        }

        if (!StringUtils.hasText(url)) {
            log.error("invalid request url");
            return;
        }

        try {
            var reqSpec = restClient.method(method)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                reqSpec.body(body);
            }

            if (StringUtils.hasText(apikey)) {
                // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
                reqSpec.header("Authorization", apikey);
            }

            log.debug("send restapi request:\nurl: {}\nbody: {}", url,
                    (body == null) ? "null" : jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));

            new Thread(() -> {
                try {
                    reqSpec.retrieve()
                        .onStatus(HttpStatusCode::is1xxInformational, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        })
                        .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                            handle.onReceived(true, request, response);
                        })
                        .onStatus(HttpStatusCode::is3xxRedirection, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                        })
                        .onStatus(HttpStatusCode::isError, (request, response) -> {
                            log.info(String.format("%s, %s, %s", request.getURI(), response.getStatusCode(), response.getHeaders()));
                            handle.onReceived(false, request, response);
                        })
                        .toBodilessEntity();
                } catch (ResourceAccessException e) {
                    logResourceAccessException(method, url, e);
                    handle.onReceived(false, null, null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }).start();
        } catch (ResourceAccessException e) {
            logResourceAccessException(method, url, e);
            handle.onReceived(false, null, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        restClientService.sendAsync(HttpMethod.POST, "http://localhost:8181/api/post/echo", "passw@rd", new RequestBody("foo", "bar", 1), (ok, body) -> {
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
        });
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

