package com.example.httpclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class WebClientAsyncTests {

    @Autowired
    @Qualifier("trustAllWebClient")
    private WebClient webClient;

    private final String serverUrl1 = "https://jsonplaceholder.typicode.com/todos";
    private final String serverUrl2 = "https://httpbin.org/"; 

    @Test
    public void getMono() {

        // var mono = webClient.get()
        // .uri(serverUrl1)
        // .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        // .retrieve()
        // .onStatus(HttpStatusCode::is2xxSuccessful, (response) -> {
        //     log.info(String.format("%s, %s", response.statusCode(), response.headers()));
        //     return reactor.core.publisher.Mono.empty();
        // }).bodyToMono(String.class);

        // String result = mono.block();
        // System.out.println(result);

        // mono.subscribe(e -> System.out.println(e));
    }

    @Test
    public void getFlux() {

        // var flux = webClient.get()
        // .uri(serverUrl1)
        // .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        // .retrieve()
        // .onStatus(HttpStatusCode::is2xxSuccessful, (response) -> {
        //     log.info(String.format("%s, %s", response.statusCode(), response.headers()));
        //     return reactor.core.publisher.Mono.empty();
        // }).bodyToFlux(String.class);

        //flux.subscribe(e -> System.out.println(e));
    }
}
