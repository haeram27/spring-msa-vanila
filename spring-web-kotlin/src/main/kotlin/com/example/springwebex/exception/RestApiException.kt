package com.example.springwebex.exception

import com.example.springwebex.exception.ecode.ErrorCode

data class RestApiException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.defaultMessage,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {

    constructor(errorCode: ErrorCode) : this(errorCode, errorCode.defaultMessage, null)

    constructor(errorCode: ErrorCode, cause: Throwable) : this(errorCode, errorCode.defaultMessage, cause)
}
