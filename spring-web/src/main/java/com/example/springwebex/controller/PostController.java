package com.example.springwebex.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.springwebex.model.restreq.BasicReqDto;
import com.example.springwebex.model.restreq.MongoCommonFindReqDto;
import com.example.springwebex.model.restresp.ResponseJsonDto;
import com.example.springwebex.service.MongoCommonFindService;
import com.example.springwebex.service.PostEchoService;
import com.example.springwebex.util.ServletUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/post",
                consumes = MediaType.APPLICATION_JSON_VALUE,  // verify "Accept" Header of Request
                produces = MediaType.APPLICATION_JSON_VALUE)  // set "Content-Type" Header of Response
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final JsonMapper mapper;
    private final MongoCommonFindService mongoCommonFindService;
    private final PostEchoService postEchoService;

    @PostMapping("/empty")
    public JsonNode empty(
            @RequestBody(required = false) JsonNode requestBody,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("API - /empty");
        log.info("client.ip : {}", clientIp);
        log.info("request :\n{}", requestBody.toPrettyString());

        return mapper.createObjectNode();
    }

// @formatter:off
/*
curl 'http://localhost:8181/api/post/echo' \
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
    @PostMapping("/echo")
    public JsonNode echo(
            @RequestBody(required = false) JsonNode requestBody,
            HttpServletRequest httpServletRequest) {

        log.info("API - /api/post/echo");

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("client.ip : {}, ", clientIp);
        log.info("request :\n{}", requestBody.toPrettyString());

        String respStr = requestBody.toString();

        JsonNode responseBody = mapper.createObjectNode();
        if (StringUtils.hasLength(respStr)) {
            try {
                responseBody = mapper.readTree(respStr);
            } catch (Exception e) {
                log.error("## Error", e);
            }
        }

        return responseBody;
    }

// @formatter:off
/*
curl 'http://localhost:8181/api/post/customdto/echo' \
-H "Content-Type: application/json;charset=UTF-8" \
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
    @PostMapping("/customdto/echo")
    public ResponseJsonDto<?> echo(
            @RequestBody BasicReqDto requestBody,
            HttpServletRequest httpServletRequest) {

        log.info("API - /api/post/customdto/echo");

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("client.ip : {}, ", clientIp);
        log.info("request :\n{}", mapper.valueToTree(requestBody).toPrettyString());

        return postEchoService.echo(requestBody);
    }

    @PostMapping("/mongo/find")
    public ResponseJsonDto<List<Map<String, Object>>> find(
            @RequestBody MongoCommonFindReqDto requestBody,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("client.ip : {}, ", clientIp);
        log.info("request :\n{}", mapper.valueToTree(requestBody).toPrettyString());

        return mongoCommonFindService.find(requestBody);
    }

// @formatter:off
/*
curl 'http://localhost:8181/api/post/resource/file' \
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
    @PostMapping("/resource/file")
    public JsonNode file(
            @RequestBody(required = false) JsonNode requestBody,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("API - /file");
        log.info("client.ip : {}, request : {}", clientIp, requestBody.toPrettyString());

        String respStr = "";
        try {
            respStr = new String(
                new ClassPathResource("resp-sample.json").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            log.error("## Error", e);
        }

        JsonNode responseBody = mapper.createObjectNode();
        if (StringUtils.hasLength(respStr)) {
            try {
                responseBody = mapper.readTree(respStr);
            } catch (Exception e) {
                log.error("## Error", e);
            }
        }

        return responseBody;
    }
}