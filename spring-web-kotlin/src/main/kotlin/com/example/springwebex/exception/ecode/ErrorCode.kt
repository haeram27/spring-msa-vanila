package com.example.springwebex.exception.ecode

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ErrorCode(
    val code: String,
    val defaultMessage: String
) {
    SUCCESS("0", "Success"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error"),
    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not Found"),
    CONFLICT("409", "Conflict"),
    UNSUPPORTED_MEDIA_TYPE("415", "Unsupported Media Type"),
    VALIDATION_ERROR("422", "Validation Error"),
    UNKNOWN("999", "Unknown Error");

    @JsonValue
    fun getValue(): String = code

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): ErrorCode {
            return entries.find { it.code == value } ?: UNKNOWN
        }
    }
}
