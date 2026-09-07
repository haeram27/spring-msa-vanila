package com.example.springwebex.controller

import java.nio.charset.StandardCharsets
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.example.springwebex.model.restreq.BasicReqDto
import com.example.springwebex.model.restreq.MongoCommonFindReqDto
import com.example.springwebex.model.restresp.ResponseJsonDto
import com.example.springwebex.service.MongoCommonFindService
import com.example.springwebex.service.PostEchoService
import com.example.springwebex.util.ServletUtil
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping(
    "/api/post",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class PostController(
    private val mapper: JsonMapper,
    private val mongoCommonFindService: MongoCommonFindService,
    private val postEchoService: PostEchoService
) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/empty")
    fun empty(
        @RequestBody(required = false) requestBody: JsonNode?,
        httpServletRequest: HttpServletRequest
    ): JsonNode {
        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "API - /empty" }
        log.info { "client.ip: $clientIp" }
        log.info { "request:\n${requestBody?.toPrettyString() ?: "null"}" }

        return mapper.createObjectNode()
    }

    @PostMapping("/echo")
    fun echo(
        @RequestBody(required = false) requestBody: JsonNode?,
        httpServletRequest: HttpServletRequest
    ): JsonNode {
        log.info { "api - /api/post/echo" }

        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "client.ip: $clientIp" }
        log.info { "request.body:\n${requestBody?.toPrettyString() ?: "null"}" }

        val respStr = requestBody?.toString() ?: ""

        var responseBody: JsonNode = mapper.createObjectNode()
        if (StringUtils.hasLength(respStr)) {
            try {
                responseBody = mapper.readTree(respStr)
            } catch (e: Exception) {
                log.error(e) { "## Error" }
            }
        }

        return responseBody
    }

    @PostMapping("/customdto/echo")
    fun echoCustomDto(
        @RequestBody requestBody: BasicReqDto,
        httpServletRequest: HttpServletRequest
    ): ResponseJsonDto<BasicReqDto> {
        log.info { "api - /api/post/customdto/echo" }

        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "client.ip: $clientIp" }
        log.info { "request.body:\n${mapper.valueToTree<tools.jackson.databind.JsonNode>(requestBody).toPrettyString()}" }

        return postEchoService.echo<BasicReqDto>(requestBody)
    }

    @PostMapping("/mongo/find")
    fun find(
        @RequestBody requestBody: MongoCommonFindReqDto,
        httpServletRequest: HttpServletRequest
    ): ResponseJsonDto<List<Map<String, Any>>> {
        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "client.ip: $clientIp" }
        log.info { "request.body:\n${mapper.valueToTree<tools.jackson.databind.JsonNode>(requestBody).toPrettyString()}" }

        @Suppress("UNCHECKED_CAST")
        return mongoCommonFindService.find(requestBody)
    }

    @PostMapping("/resource/file")
    fun file(
        @RequestBody(required = false) requestBody: JsonNode?,
        httpServletRequest: HttpServletRequest
    ): JsonNode {
        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "api - /file" }
        log.info { "client.ip: $clientIp" }
        log.info { "request.body:\n${requestBody?.toPrettyString() ?: "null"}" }

        var respStr = ""
        try {
            respStr = String(
                ClassPathResource("resp-sample.json").inputStream.readAllBytes(),
                StandardCharsets.UTF_8
            )
        } catch (e: Exception) {
            log.error(e) { "## Error" }
        }

        var responseBody: JsonNode = mapper.createObjectNode()
        if (StringUtils.hasLength(respStr)) {
            try {
                responseBody = mapper.readTree(respStr)
            } catch (e: Exception) {
                log.error(e) { "## Error" }
            }
        }

        return responseBody
    }
}
