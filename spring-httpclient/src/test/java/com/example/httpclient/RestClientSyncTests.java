package com.example.httpclient;

import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class RestClientSyncTests {

    @Autowired
    @Qualifier("trustAllRestClient")
    private RestClient restClient;

    private final String serverUrl1 = "https://jsonplaceholder.typicode.com/posts";
    private final String serverUrl2 = "https://httpbin.org/"; 

    @Test
    public void run() {

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
}
