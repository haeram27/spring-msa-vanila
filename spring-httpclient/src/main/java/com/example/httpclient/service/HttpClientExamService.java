package com.example.httpclient.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpClientExamService {
    private final RestClient trustAllRestClient;

    private final String url = "https://jsonplaceholder.typicode.com/posts";

    public void sendSyncReq() {
        var future = trustAllRestClient.post()
        .uri(url)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(String.class);

        System.out.println(future);
    }
}
