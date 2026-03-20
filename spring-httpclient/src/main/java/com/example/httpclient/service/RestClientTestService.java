package com.example.httpclient.service;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.example.httpclient.service.restclient.RestClientService;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestClientTestService {
    private final JsonMapper jsonMapper;
    private final RestClientService restClientService;

    private final String url = "http://localhost:8181/api/post/echo";

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

        restClientService.sendAsync(HttpMethod.POST, url, "passw@rd", new RequestBody("foo", "bar", 1), (ok, body) -> {
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
}
