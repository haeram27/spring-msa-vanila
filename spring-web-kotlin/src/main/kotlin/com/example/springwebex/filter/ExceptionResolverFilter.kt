package com.example.springwebex.filter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import io.github.oshai.kotlinlogging.KotlinLogging

@Component
class ExceptionResolverFilter : Filter {

    private val log = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    override fun init(filterConfig: FilterConfig?) {
        log.debug { "ExceptionResolverFilter initialized" }
    }

    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain?
    ) {
        try {
            chain?.doFilter(request, response)
        } catch (e: Exception) {
            log.error(e) { "Exception occurred in filter" }
            val httpResponse = response as? HttpServletResponse
            if (httpResponse != null) {
                httpResponse.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                httpResponse.contentType = "application/json"
                try {
                    val errorResponse = mapOf(
                        "error" to "Internal Server Error",
                        "message" to (e.message ?: "An error occurred"),
                        "timestamp" to System.currentTimeMillis()
                    )
                    httpResponse.writer.write(objectMapper.writeValueAsString(errorResponse))
                } catch (ioe: Exception) {
                    log.error(ioe) { "Error writing response" }
                }
            }
        }
    }

    override fun destroy() {
        log.debug { "ExceptionResolverFilter destroyed" }
    }
}
