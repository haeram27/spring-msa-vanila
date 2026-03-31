package com.example.httpclient.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class JdkRestClientTests {

    private JdkRestClient client = JdkRestClient.INSTANCE;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void count() {
        var url = "http://localhost:8181/api/pagenation/servers/count";
        var response = client.post(url, null, "{}");
        log.info("response=\n{}", new String(response.body));
    }

    @Test
    public void pagenation() {
        var countUrl = "http://localhost:8181/api/pagenation/servers/count";
        var serverUrl = "http://localhost:8181/api/pagenation/servers";

        var response = client.post(countUrl, null, "{}");
        log.info("response=\n{}", new String(response.body));

        int totalCount = 0;
        try {
            totalCount = jsonMapper.readTree(response.body).at("/body/totalCount").asInt();
            log.info("totalCount={}", totalCount);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        var result = client.postAllPages(serverUrl, null, "{\"pageNumber\":%d,\"pageSize\":%d}", totalCount, totalCount/5, 3000);
        log.info("result.size={}", result.size());
        result.forEach((k, v) -> {
            try {
                //log.info("page={}, body=\n{}", k, jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readTree(v)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
