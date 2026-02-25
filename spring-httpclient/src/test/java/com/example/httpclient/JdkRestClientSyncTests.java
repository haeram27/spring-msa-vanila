package com.example.httpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.example.EvaluatedTimeTests;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
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
public class JdkRestClientSyncTests extends EvaluatedTimeTests {

    @Autowired
    @Qualifier("jdkClientTrustAllRestClient")
    private RestClient restClient;

    @Autowired
    @Qualifier("restClientJsonMapper")
    private JsonMapper mapper;

    // https://jsonplaceholder.typicode.com/guide/
    private final String urlNotExist = "http://localhost.com";
    private final String urlGetTodoAll = "https://jsonplaceholder.typicode.com/todos";
    private final String urlGetTodoOne = "https://jsonplaceholder.typicode.com/todos/1";
    private final String serverUrl2 = "https://httpbin.org/"; 

    @Test
    public void getResponseMapToJsonNode() {

        log.info("uri="+urlGetTodoOne);

        JsonNode responseBody = mapper.createObjectNode();
        try {
            responseBody = restClient.get()
            .uri(urlNotExist)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
                log.info(m);
            })
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
                log.error(m);
            })
            .body(JsonNode.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        // handle responseBody
        if (responseBody.isEmpty()) {
            log.error("empty response");
            return;
        }

        try {
            log.info("response:\n" + responseBody.toPrettyString());
        } catch (Exception e) {
            log.error("#Error", e);
        }
    }

    // Recommneded Map Response to JsonNode
    @Test
    public void postResponseMapToJsonNode() {

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

        JsonNode responseBody = mapper.createObjectNode();
        try {
            responseBody = restClient.post()
            .uri("http://localhost:8181/api/v1/config/CIs/servers")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new RequestBody2("a", "b", 2))
            .retrieve()
            .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
                log.info(m);
            })
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
                log.error(m);
            })
            .body(JsonNode.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        // handle responseBody
        if (responseBody.isEmpty()) {
            log.error("empty response");
            return;
        }

        try {
            log.info("response:\n" + responseBody.toPrettyString());
        } catch (Exception e) {
            log.error("#Error", e);
        }
    }

    // Only for test
    // map response to String waste memory when response size is too big(dozens MB)
    @Test
    public void postResponseMapToString() {

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

        String resonseBody = "";
        try {
            resonseBody = restClient.post()
            .uri("https://jsonplaceholder.typicode.com/posts")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new RequestBody2("a", "b", 2))
            .retrieve()
            .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
                log.info(m);
            })
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                var m = String.format("%s, %s", response.getStatusCode(), response.getHeaders());
                log.error(m);
            })
            .body(String.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        // handle resonseBody string
        if (!StringUtils.hasText(resonseBody)) {
            log.error("empty response");
            return;
        }

        log.info("response: "+resonseBody);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            var jsonNode = mapper.readTree(resonseBody);

            if (jsonNode == null || jsonNode.isEmpty()) {
                log.error("empty body");
                return;
            }

            if (jsonNode.isObject()) {
                log.debug("resonseBody is object body type");

                ObjectNode node = (ObjectNode) jsonNode;
                Map<String, Object> map = mapper.convertValue(node, new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>() {});
                // var map = mapper.treeToValue(node, Map.class);
                list.add(map);
            } else if (jsonNode.isArray()) {
                log.debug("resonseBody is array body type");

                ArrayNode node = (ArrayNode) jsonNode;
                list = mapper.convertValue(node, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String,Object>>>() {});
            } else {
                log.error("response content type is NOT applicable");
            }
        } catch (Exception e) {
            log.error("response parsing error", e);
        }
    }
}
