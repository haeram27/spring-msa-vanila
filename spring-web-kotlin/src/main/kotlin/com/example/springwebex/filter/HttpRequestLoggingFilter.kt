package com.example.springwebex.filter

import org.springframework.stereotype.Component
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import io.github.oshai.kotlinlogging.KotlinLogging

@Component
class HttpRequestLoggingFilter : Filter {

    private val log = KotlinLogging.logger {}

    override fun init(filterConfig: FilterConfig?) {
        log.debug { "HttpRequestLoggingFilter initialized" }
    }

    override fun doFilter(
        request: ServletRequest?,
        response: ServletResponse?,
        chain: FilterChain?
    ) {
        val httpRequest = request as? HttpServletRequest
        if (httpRequest != null) {
            log.debug { "========== Request Start ==========" }
            log.debug { "Method: ${httpRequest.method}" }
            log.debug { "URI: ${httpRequest.requestURI}" }
            log.debug { "URL: ${httpRequest.requestURL}" }
            log.debug { "QueryString: ${httpRequest.queryString}" }
            
            val headerNames = httpRequest.headerNames
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    val headerName = headerNames.nextElement()
                    val headerValue = httpRequest.getHeader(headerName)
                    log.debug { "Header - $headerName: $headerValue" }
                }
            }
            val attributeNames = httpRequest.attributeNames
            if (attributeNames != null) {
                while (attributeNames.hasMoreElements()) {
                    val attributeName = attributeNames.nextElement()
                    val attributeValue = httpRequest.getAttribute(attributeName)
                    log.debug { "Attribute - $attributeName: $attributeValue" }
                }
            }

            log.debug { "========== Request End ==========" }
        }

        chain?.doFilter(request, response)
    }

    override fun destroy() {
        log.debug { "HttpRequestLoggingFilter destroyed" }
    }
}
