package com.example.httpclient;

import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class RestClientSyncTests {

    @Autowired
    @Qualifier("trustAllRestClient")
    private RestClient restClient;

    @Autowired
    @Qualifier("trustAllWebClient")
    private WebClient webClient;

    private final String serverUrl1 = "https://jsonplaceholder.typicode.com/posts";
    private final String serverUrl2 = "https://httpbin.org/"; 

    @Test
    public void sync() {

        var future = restClient.post()
        .uri(serverUrl1)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
            var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
            log.info(m);
        })
        .body(String.class);

        System.out.println(future);
    }

    @Test
    public void async() {

        var mono = webClient.post()
        .uri(serverUrl1)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatusCode::is2xxSuccessful, (response) -> {
            log.info(String.format("%s, %s", response.statusCode(), response.headers()));
            return reactor.core.publisher.Mono.empty();
        }).bodyToMono(String.class);

        // try {
        //     mono.toFuture().getNow("heelo");
        // } catch (CancellationException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // } catch (CompletionException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // } catch (Exception e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }
}
