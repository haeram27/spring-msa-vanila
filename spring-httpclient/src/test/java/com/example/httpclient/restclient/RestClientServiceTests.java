package com.example.httpclient.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import com.example.EvaluatedTimeTests;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * RestTemplate Tests
 *
 * Response mapping recommendation order
 * - DTO
 * - JsonNode
 * - String (inefficient at memorry)
 */

@Slf4j
@SpringBootTest
public class RestClientServiceTests extends EvaluatedTimeTests {

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private RestClientService client;

    // https://jsonplaceholder.typicode.com/guide/
    private final String urlNotExist = "http://localhost.com";
    private final String urlGetTodoAll = "https://jsonplaceholder.typicode.com/todos";
    private final String urlGetTodoOne = "https://jsonplaceholder.typicode.com/todos/1";
    private final String urlPostJsonPlaceHolder = "https://jsonplaceholder.typicode.com/posts";
    private final String serverUrl2 = "https://httpbin.org/"; 

    @Test
    public void postLocalSerer() {

        log.info("uri="+urlGetTodoOne);

        // var requestBody = """
        //     {
        //         userId: 10
        //         id: 101
        //         title: 'foo',
        //         body: 'bar',
        //     }
        //     """;
        record RequestBody(
            Integer userId,
            Integer id,
            String title,
            String body
            ) {}

        client.send(HttpMethod.POST,
            "http://localhost:8181/api/post/echo",
            "passw@rd",
            new RequestBody(10, 101, "foo", "bar"),
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

    @Test
    public void postJsonPlaceHolder() {

        log.info("uri="+urlGetTodoOne);

        // var requestBody = """
        //     {
        //         userId: 10
        //         id: 101
        //         title: 'foo',
        //         body: 'bar',
        //     }
        //     """;
        record RequestBody(
            Integer userId,
            Integer id,
            String title,
            String body
            ) {}

        client.send(HttpMethod.POST,
            urlPostJsonPlaceHolder,
            "passw@rd",
            new RequestBody(10, 101, "foo", "bar"),
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


/*
    public void postWithClassTest() {

        log.info("getServers()");

        client.sendAsync(HttpMethod.POST,
            "http://localhost:8181/api/v1/datas",
            "passw@rd",
            "{}",
            (ok, body) -> {
                if (ok) {
                    log.info("success");
                    var response = jsonMapper.convertValue(body, MyResponse.class);
                    var data = response.getBody().getData();
                    var datas = jsonMapper.convertValue(data, new TypeReference<List<MyData>>(){});

                    datas.forEach(d -> log.info(d.toString()));
                } else {
                    log.info("failed");
                }
            }
        );
    }
*/
}
