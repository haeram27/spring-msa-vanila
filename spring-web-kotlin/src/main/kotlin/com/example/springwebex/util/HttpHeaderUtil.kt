package com.example.springwebex.util

import java.nio.charset.StandardCharsets
import java.util.*

object HttpHeaderUtil {

    private const val BEARER_TYPE = "Bearer"

    fun getToken(authorization: Enumeration<String>): String? {
        while (authorization.hasMoreElements()) {
            val value = authorization.nextElement()
            if (StringUtil.toLowerCase(value)?.startsWith(StringUtil.toLowerCase(BEARER_TYPE) ?: "") == true) {
                var authHeaderValue = value.substring(BEARER_TYPE.length).trim()
                val commaIndex = authHeaderValue.indexOf(',')
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex)
                }
                return authHeaderValue
            }
        }
        return null
    }

    fun getBase64DecodedToken(authorization: String?): String? {
        if (authorization != null && StringUtil.toLowerCase(authorization)?.startsWith(StringUtil.toLowerCase(BEARER_TYPE) ?: "") == true) {
            var authHeaderValue = authorization.substring(BEARER_TYPE.length).trim()
            val commaIndex = authHeaderValue.indexOf(',')
            if (commaIndex > 0) {
                authHeaderValue = authHeaderValue.substring(0, commaIndex)
            }
            return String(
                Base64.getDecoder().decode(authHeaderValue.toByteArray(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
            )
        }
        return null
    }
}
