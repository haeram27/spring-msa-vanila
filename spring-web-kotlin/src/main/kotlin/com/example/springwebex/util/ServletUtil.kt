package com.example.springwebex.util

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import jakarta.servlet.http.HttpServletRequest
import io.github.oshai.kotlinlogging.KotlinLogging
object ServletUtil {

    private val log = KotlinLogging.logger {}

    fun getClientIp(httpServletRequest: HttpServletRequest): String {
        var ip: String? = httpServletRequest.getHeader("X-FORWARDED-FOR")
        if (StringUtil.isEmpty(ip)) {
            ip = httpServletRequest.getHeader("Proxy-Client-IP")
        }
        if (StringUtil.isEmpty(ip)) {
            ip = httpServletRequest.getHeader("WL-Proxy-Client-IP")
        }
        if (StringUtil.isEmpty(ip)) {
            ip = httpServletRequest.remoteAddr
        }
        return ip ?: ""
    }

    fun getClientIp(): String {
        return getClientIp(
            ((RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request)
        )
    }

    fun getLoginId(): String {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        return if (request.getAttribute("adminId") != null) {
            request.getAttribute("adminId").toString()
        } else {
            ""
        }
    }
}
