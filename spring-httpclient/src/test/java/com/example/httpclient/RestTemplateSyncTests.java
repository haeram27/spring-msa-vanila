package com.example.httpclient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;
/*
 * RestAPI Client GET Test
 */

@Slf4j
@SpringBootTest
public class RestTemplateSyncTests {

    @Autowired
    @Qualifier("trustAllRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("restClientObjectMapper")
    private JsonMapper mapper;

    // https://jsonplaceholder.typicode.com/todos
    private static final String API_URL_DOMAIN = "jsonplaceholder.typicode.com";
    private static final String API_URL_PATH = "/todos";
    private static final String TEST_BEARER_AUTH_TOKEN = "";

    public List<Map<String, Object>> send(HttpMethod method, URI uri, HttpEntity<Void> httpEntity) {
        // ResponseEntity<JsonNode> responseEntity;
        ResponseEntity<String> responseEntity;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();;

        if (Strings.isEmpty(method.name())
            || uri == null
            || httpEntity == null) {
            log.error("Error: No content");
            return list;
        }

        try {
            log.debug("## request uri : {}", uri);

            // responseEntity = restTemplate.exchange(uri, method, httpEntity, JsonNode.class);
            responseEntity = restTemplate.exchange(uri, method, httpEntity, String.class);

            log.debug("Response Body: " + responseEntity.getBody());
            log.debug("Status Code: " + responseEntity.getStatusCode());
            log.debug("Headers: " + responseEntity.getHeaders());
            log.debug("Response http code: {}, reason: {}",
                    responseEntity.getStatusCode().value(),
                    HttpStatus.valueOf(responseEntity.getStatusCode().value()).getReasonPhrase());

            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                var body = responseEntity.getBody();
                if (body == null) body = "";

                var responseBody = mapper.readTree(body);
                if (responseBody == null || responseBody.isEmpty()) {
                    log.error("Error: Empty Body");
                }

                if (responseBody.isObject()) {
                    log.debug("resonseBody is object body type");

                    ObjectNode node = (ObjectNode) responseBody;
                    Map<String, Object> map = mapper.convertValue(node, new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>() {});
                    // var map = mapper.treeToValue(node, Map.class);
                    list.add(map);
                } else if (responseBody.isArray()) {
                    log.debug("resonseBody is array body type");

                    ArrayNode node = (ArrayNode) responseBody;
                    list = mapper.convertValue(node, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String,Object>>>() {});
                } else {
                    log.error("Error: Response content type is NOT applicable");
                }
            } else if (responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                log.error("Error: No content");
            } else {
                log.error("Error: Http Server Internal Error");
            }
        } catch (RestClientResponseException ex) {
            log.error("Error: {}", ex.getMessage());

            var status = ex.getStatusCode();
            log.debug("Status Code: " + status);
            // log.debug("code value: " + status.value());
            // log.debug("status text: " + ex.getStatusText());
            log.debug("Response Body: " + ex.getResponseBodyAsString());
            log.debug("Headers: " + ex.getResponseHeaders());

            if (status.is4xxClientError()) {
                log.error("Error: {}", ex.getResponseBodyAsString());

                // if required handle with exact 4XX Error
                if (status.equals(HttpStatus.BAD_REQUEST)) {
                    log.error("Error: BAD_REQUEST: {}", ex.getResponseBodyAsString());
                } else if (status.equals(HttpStatus.UNAUTHORIZED)) {
                    log.error("Error: UNAUTHORIZED: {}", ex.getResponseBodyAsString());
                } else if (status.equals(HttpStatus.FORBIDDEN)) {
                    log.error("Error: FORBIDDEN: {}", ex.getResponseBodyAsString());
                } else if (status.equals(HttpStatus.NOT_FOUND)) {
                    log.error("Error: NOT_FOUND: {}", ex.getResponseBodyAsString());
                } else if (status.equals(HttpStatus.I_AM_A_TEAPOT)) {
                    log.error("Error: I_AM_A_TEAPOT: {}", ex.getResponseBodyAsString());
                } else if (status.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                    log.error("Error: TOO_MANY_REQUESTS: {}", ex.getResponseBodyAsString());
                }
            } else if (status.is5xxServerError()) {
                log.error("Error: {}", ex.getResponseBodyAsString());
            } else {
                log.error("Error: {}", ex.getResponseBodyAsString());
            }
        } catch (ResourceAccessException ex) {
            log.error("Error: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage());
        }

        return list;
    }

    @Test
    public void getTest() {
        var uri = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host(API_URL_DOMAIN)
                    .path(API_URL_PATH)
                    //.queryParam("key", "value")
                    .encode().build().toUri();

        var headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // headers.setContentType(MediaType.APPLICATION_JSON);

        if (Strings.isNotEmpty(TEST_BEARER_AUTH_TOKEN))
            headers.setBearerAuth(TEST_BEARER_AUTH_TOKEN);

        var httpEntity = new HttpEntity<Void>(headers);
        var responseBody = send(HttpMethod.GET, uri, httpEntity);

        if (responseBody != null && responseBody.size() > 0) {
            try {
                log.debug("## response: {}",
                        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseBody));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("## invalid response");
            }
        } else {
            log.error("## empty response");
        }
    }
}