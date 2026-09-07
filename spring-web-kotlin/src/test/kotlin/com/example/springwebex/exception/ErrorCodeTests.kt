package com.example.springwebex.exception

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ErrorCodeTests {

    @Test
    fun testErrorCodeValues() {
        assertEquals("500", ErrorCode.INTERNAL_SERVER_ERROR.code)
        assertEquals("400", ErrorCode.BAD_REQUEST.code)
        assertEquals("401", ErrorCode.UNAUTHORIZED.code)
    }

    @Test
    fun testErrorCodeFromValue() {
        val errorCode = ErrorCode.fromValue("500")
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, errorCode)
    }

    @Test
    fun testErrorCodeFromUnknownValue() {
        val errorCode = ErrorCode.fromValue("999")
        assertEquals(ErrorCode.UNKNOWN, errorCode)
    }

    @Test
    fun testRestApiException() {
        val exception = RestApiException(ErrorCode.BAD_REQUEST)
        assertNotNull(exception.errorCode)
        assertEquals(ErrorCode.BAD_REQUEST, exception.errorCode)
    }
}
