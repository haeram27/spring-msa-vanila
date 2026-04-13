package com.example.springwebex.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.example.springwebex.model.pagenation.Server
import com.example.springwebex.util.PagenationUtil
import com.example.springwebex.util.ServletUtil
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping(
    "/api/pagenation",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class PagenationController(private val mapper: JsonMapper) {
    private val log = KotlinLogging.logger {}
    private val servers: MutableList<Server> = mutableListOf()

    init {
        for (i in 1..SERVER_SIZE) {
            servers.add(
                Server(
                    hostName = "d7d5-vac-$i",
                    ip = "1.1.1.1",
                    managerId = "NB10185_$i",
                    managerName = "홍길동",
                    developerId = "NB10185_$i",
                    developerName = "홍길동",
                    serviceName = "/a/b"
                )
            )
        }
    }

    @PostMapping("/empty")
    fun empty(
        @RequestBody(required = false) requestBody: JsonNode?,
        httpServletRequest: HttpServletRequest
    ): JsonNode {
        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "api - /empty()" }
        log.info { "client.ip : $clientIp" }
        log.info { "request.body :\n${requestBody?.toPrettyString() ?: "null"}" }

        return mapper.createObjectNode()
    }

    @PostMapping("/servers")
    fun servers(
        @RequestBody(required = false) requestBody: JsonNode?,
        httpServletRequest: HttpServletRequest
    ): JsonNode {
        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "api - /servers()" }
        log.info { "client.ip : $clientIp" }
        if (requestBody != null) {
            log.info { "request.body :\n${requestBody.toPrettyString()}" }
        } else {
            log.info { "request.body is null" }
        }

        val pageNumber = requestBody?.path("pageNumber")?.asInt(0) ?: 0
        val pageSize = requestBody?.path("pageSize")?.asInt(0) ?: 0

        val totalServerCount = servers.size
        val collectedServers = PagenationUtil.pagenationList(servers, pageSize, pageNumber)
        val respStr = """
            {"header":{"isSuccessful":true,"resultCode":0,"resultMessage":"SUCCESS"},"body":{"pageNum":0,"pageSize":0,"totalCount":0,"data":null}}
        """.trimIndent()

        var responseBody: JsonNode = mapper.createObjectNode()
        try {
            responseBody = mapper.readTree(respStr) as ObjectNode
            val body = responseBody.path("body") as ObjectNode
            body.put("pageNum", pageNumber)
            body.put("pageSize", pageSize)
            body.put("totalCount", totalServerCount)
            body.set("data", mapper.valueToTree(collectedServers))
        } catch (e: Exception) {
            log.error(e) { "## Error" }
        }

        return responseBody
    }

    @PostMapping("/servers/count")
    fun count(
        @RequestBody(required = false) requestBody: JsonNode?,
        httpServletRequest: HttpServletRequest
    ): JsonNode {
        val clientIp = ServletUtil.getClientIp(httpServletRequest)
        log.info { "api - /servers/count()" }
        log.info { "client.ip : $clientIp" }
        if (requestBody != null) {
            log.info { "request.body :\n${requestBody.toPrettyString()}" }
        } else {
            log.info { "request.body is null" }
        }

        var responseBody: JsonNode = mapper.createObjectNode()
        if (StringUtils.hasLength(COUNT_RESPONSE)) {
            try {
                val json = mapper.readTree(COUNT_RESPONSE)
                if (!json.path("body").path("totalCount").isMissingNode) {
                    val node = json.path("body") as ObjectNode
                    node.put("totalCount", servers.size)
                }
                responseBody = json
            } catch (e: Exception) {
                log.error(e) { "## Error" }
            }
        }

        return responseBody
    }

    companion object {
        private const val SERVER_SIZE = 570
        private const val COUNT_RESPONSE = """
            {"header":{"isSuccessful":true,"resultCode":0,"resultMessage":"SUCCESS"},"body":{"totalCount":1,"data":null}}
        """
    }
}
