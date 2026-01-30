package com.example.httpclient;

import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class RestClientSyncTests {

    @Autowired
    @Qualifier("apacheClientTrustAllRestClient")
    private RestClient restClient;

    // https://jsonplaceholder.typicode.com/guide/
    private final String serverUrl1 = "https://jsonplaceholder.typicode.com/todos";
    private final String serverUrl2 = "https://httpbin.org/"; 

    @Test
    public void get() {

        log.info("uri="+serverUrl1);

        var body = restClient.get()
        .uri(serverUrl1)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
            var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
            log.info(m);
        })
        .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
            var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
            log.error(m);
        })
        .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
            var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
            log.error(m);
        })
        .body(String.class);

        System.out.println(body);
    }

    @Test
    public void post() {

        // var requestBody = """
        //     {
        //         title: 'foo',
        //         body: 'bar',
        //         userId: 1
        //     }
        //     """;

        @Data
        class RequestBody {
            String title;
            String body;
            Integer userId;
        }

        var requestBody = new RequestBody();
        requestBody.title = "foo";
        requestBody.body = "bar";
        requestBody.userId = 1;

        record RequestBody2(
            String title,
            String body,
            Integer userId) {
        }

        var body = restClient.post()
            .uri("https://jsonplaceholder.typicode.com/posts")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new RequestBody2("a", "b", 2))
            .retrieve()
            .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                log.info("=== request ===============================================================");
                log.info(String.format("URI: %s", request.getURI()));
                log.info(String.format("Method: %s", request.getMethod()));
                log.info(String.format("Headers: %s", request.getHeaders()));

                log.info("=== response ===============================================================");
                log.info(String.format("%s, %s", response.getStatusCode(), response.getHeaders()));
            })
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                log.info("=== request ===============================================================");
                log.info(String.format("URI: %s", request.getURI()));
                log.info(String.format("Method: %s", request.getMethod()));
                log.info(String.format("Headers: %s", request.getHeaders()));

                log.info("=== response ===============================================================");
                log.error(String.format("%s, %s", response.getStatusCode(), response.getHeaders()));
            })
            .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                log.info("=== request ===============================================================");
                log.info(String.format("URI: %s", request.getURI()));
                log.info(String.format("Method: %s", request.getMethod()));
                log.info(String.format("Headers: %s", request.getHeaders()));

                log.info("=== response ===============================================================");
                log.error(String.format("%s, %s", response.getStatusCode(), response.getHeaders()));
            })
            .body(String.class);

        System.out.println(body);
    }
}
