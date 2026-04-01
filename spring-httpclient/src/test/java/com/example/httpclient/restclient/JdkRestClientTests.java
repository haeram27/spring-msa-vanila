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

        String requestBody = """
        {
            "pageNumber": %d,
            "pageSize": %d
        }
        """;

        var results = client.postAllPages(serverUrl, null, requestBody, totalCount, totalCount/5, 3000);
        long count = results.values().stream().filter(b -> b.length > 0).count();

        log.info("received {} pages of servers", count);
        if (count <= 0) {
            log.error("can NOT receive any server list");
            return;
        }

        results.forEach((k, v) -> {
            if (v.length > 0) {
                // log.info("page={}, body=\n{}", k, new String(v));
            } else {
                log.warn("page={}, body is empty", k);
            }
        });

        // Do something with the results, e.g., parse JSON and process server lists
        // results.values().stream().filter(b -> b.length > 0).forEach(b -> {
        //     try {
        //         log.info("body=\n{}", jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readTree(b)));
        //     } catch (Exception e) {
        //         log.error(e.getMessage(), e);
        //     }
        // });
    }
}
