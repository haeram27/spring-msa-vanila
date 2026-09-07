package com.example.springwebex.controller;


    

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springwebex.model.pagenation.Server;
import com.example.springwebex.util.PagenationUtil;
import com.example.springwebex.util.ServletUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/pagenation",
                consumes = MediaType.APPLICATION_JSON_VALUE,  // verify "Accept" Header of Request
                produces = MediaType.APPLICATION_JSON_VALUE)  // set "Content-Type" Header of respStr
@Slf4j
public class PagenationController {

    private static final int SERVER_SIZE = 570;

    private final JsonMapper mapper;

    private List<Server> servers = new ArrayList<Server>();

    public PagenationController(JsonMapper mapper) {
        this.mapper = mapper;
        for (int i = 1; i<=SERVER_SIZE; i++) {
            servers.add(
                new Server(
                    "d7d5-vac-" +i,
                    "1.1.1.1",
                    "NB10185_" +i,
                    "홍길동",
                    "NB10185_" + i,
                    "홍길동",
                    "/a/b"
                ));
        }
    }

    @PostMapping("/empty")
    public JsonNode empty(
            @RequestBody(required = false) JsonNode requestBody,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("api - /empty()");
        log.info("client.ip : {}", clientIp);
        log.info("request.body :\n{}", requestBody.toPrettyString());

        return mapper.createObjectNode();
    }

// @formatter:off
/*
curl 'http://localhost:${server.port}/api/pagenation/servers' \
-H 'Content-Type: application/json;charset=UTF-8' \
-d '
{
    "time_zone": "Asia/Seoul",
    "start_date": "2021-01-10T00:00:00",
    "end_date": "2024-12-31T00:00:00",
    "int_list": [2, 4],
    "str_list": ["hello", "world"],
    "page_size": 5,
    "page_number": 0
}' | jq
*/
// @formatter:on
    @PostMapping("/servers")
    public JsonNode servers(
            @RequestBody(required = false) JsonNode requestBody,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("api - /servers()");
        log.info("client.ip : {}", clientIp);
        if (requestBody != null) {
            log.info("request.body :\n{}", requestBody.toPrettyString());
        } else {
            log.info("request.body is null");
        }

        var pageNumber = requestBody.path("pageNumber").asInt(0);
        var pageSize = requestBody.path("pageSize").asInt(0);

        var totalServerCount = servers.size();
        var collectedServers = PagenationUtil.pagenationList(servers, pageSize, pageNumber);
        String respStr = """
        {"header":{"isSuccessful":true,"resultCode":0,"resultMessage":"SUCCESS"},"body":{"pageNum":0,"pageSize":0,"totalCount":0,"data":null}}
        """;
        // try {
        //     respStr = new String(
        //         new ClassPathResource("nhn-resp-sample.json").getInputStream().readAllBytes(),
        //         StandardCharsets.UTF_8
        //     );
        // } catch (Exception e) {
        //     log.error("## Error", e);
        // }

        JsonNode responseBody = mapper.createObjectNode();
        try {
            responseBody = mapper.readTree(respStr);
            ObjectNode body = (ObjectNode) responseBody.path("body");
            body.put("pageNum", pageNumber);
            body.put("pageSize", pageSize);
            body.put("totalCount", totalServerCount);
            body.set("data", mapper.valueToTree(collectedServers)); // 핵심
        } catch (Exception e) {
            log.error("## Error", e);
        }

        return responseBody;
    }

    static String countResponse = """
        {"header":{"isSuccessful":true,"resultCode":0,"resultMessage":"SUCCESS"},"body":{"totalCount":1,"data":null}}
        """;

    @PostMapping("/servers/count")
    public JsonNode count(
            @RequestBody(required = false) JsonNode requestBody,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("api - /servers/count()");
        log.info("client.ip : {}", clientIp);
        if (requestBody != null) {
            log.info("request.body :\n{}", requestBody.toPrettyString());
        } else {
            log.info("request.body is null");
        }

        JsonNode responseBody = mapper.createObjectNode();
        if (StringUtils.hasLength(countResponse)) {
            try {
                var json = mapper.readTree(countResponse);
                if (!json.path("body").path("totalCount").isMissingNode()) {
                    ObjectNode node = (ObjectNode )json.path("body");
                    node.put("totalCount", servers.size());
                }
                responseBody = json;
            } catch (Exception e) {
                log.error("## Error", e);
            }
        }

        return responseBody;
    }
}
