package com.example.springwebex.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/post",
                consumes = MediaType.APPLICATION_JSON_VALUE,  // verify "Accept" Header of Request
                produces = MediaType.APPLICATION_JSON_VALUE)  // set "Content-Type" Header of Response
@RequiredArgsConstructor
@Slf4j
public class PostExamController {
    private final PostEchoService postEchoService;
    private final MongoCommonFindService mongoCommonFindService;

// @formatter:off
/*
curl 'http://localhost:${server.port}/api/post/echo' \
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
    @PostMapping("/echo")
    public ResponseJsonDto<?> echo(
            @RequestBody BasicReqDto request,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("echo()... client.ip : {}, request : {}", clientIp, request);

        return postEchoService.echo(request);
    }

    @PostMapping("/mongo/find")
    public ResponseJsonDto<List<Map<String, Object>>> find(
            @RequestBody MongoCommonFindReqDto request,
            HttpServletRequest httpServletRequest) {

        String clientIp = ServletUtil.getClientIp(httpServletRequest);
        log.info("find()... client.ip : {}, request : {}", clientIp, request);

        return mongoCommonFindService.find(request);
    }
}
