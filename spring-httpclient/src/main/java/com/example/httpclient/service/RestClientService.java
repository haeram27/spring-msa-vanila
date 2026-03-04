package com.example.httpclient.service;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.httpclient.model.RestServerConfigDto;
import com.example.httpclient.util.PathUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestClientService {
    private final JsonMapper mapper;
    private final RestClient restClient;
    private final RestServerConfigDto restServerConfig;

    private final String url = "http://localhost:8181/api/post/echo";

    public void postJsonNode(@NonNull String url, String apikey) {
        // var requestBody = """
        //     {
        //         title: 'foo',
        //         body: 'bar',
        //         userId: 1
        //     }
        //     """;
        record RequestBody2(
            String title,
            String body,
            Integer userId) {
        }

        JsonNode responseBody = mapper.createObjectNode();
        try {
            var reqSpec = restClient.post()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{}");
            // .body(new RequestBody2("a", "b", 2));

            if (apikey != null && !apikey.isBlank()) {
                // reqSpec.header("Authorization", "Bearer " + Base64.getEncoder().encodeToString(apikey.getBytes(StandardCharsets.UTF_8)));
                reqSpec.header("Authorization", "Bearer " + apikey);
            }

            responseBody = reqSpec.retrieve()
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
            // log.info("response:\n" + responseBody.toPrettyString().substring(0,1000)+"...");
            // Files.writeString(
            //     PathUtil.processWorkingDirectory()
            //             .resolve("http-response-"+LocalDateTime.now().format(
            //                 DateTimeFormatter.ofPattern("uuuuMMdd_HHmmSS")).toString()+".json")
            //     , responseBody.toPrettyString());
        } catch (Exception e) {
            log.error("#Error", e);
        }
    }

        public void callApi() {
        if (restServerConfig == null) {
            log.error("can NOT read NhnServer configuration");
            return;
        }

        var url = restServerConfig.getUrl();
        var apikey = restServerConfig.getApikey();

        if (url == null || url.isBlank()) {
            log.error("invalid server url");
            return;
        }

        postJsonNode(url, apikey);
    }
}
