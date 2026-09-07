package com.example.springwebex.model.restresp

import com.example.springwebex.exception.ecode.ErrorCode
import com.example.springwebex.util.JsonUtil
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("errorCode", "errorMsg", "response")
data class ResponseJsonDto<T>(
    @JsonProperty("errorCode")
    var errorCode: String = SUCCESS_CODE,

    @JsonProperty("errorMsg")
    var errorMsg: String = ErrorCode.SUCCESS.defaultMessage,

    @JsonProperty("response")
    var response: T? = null
) {
    fun isSuccess(): Boolean = SUCCESS_CODE == errorCode

    override fun toString(): String {
        return try {
            JsonUtil.serialize(this)
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private val SUCCESS_CODE = ErrorCode.SUCCESS.code.toString()

        fun <T> from(codeString: String, msg: String): ResponseJsonDto<T> {
            return ResponseJsonDto(
                errorCode = codeString,
                errorMsg = msg
            )
        }

        fun <T> from(e: Exception): ResponseJsonDto<T> {
            return from("", e.message ?: "")
        }
    }
}
