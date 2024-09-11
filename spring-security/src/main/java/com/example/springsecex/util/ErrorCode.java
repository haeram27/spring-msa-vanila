package com.example.springsecex.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorCode {
// @formatter:off
    SUCCESS(0, "success"),
    NOT_IMPLEMENTED(9998, "not implemented"),
    FAIL(9999, "failed"),
    UNKNOWN(10000, "unknown error");
// @formatter:on

    private final int code;
    private final String defaultMessage;

    ErrorCode(final int code, final String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @JsonValue
    public int getCode() {
        return this.code;
    }

    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    @JsonCreator
    public static ErrorCode valueOf(final int code) {
        for (final ErrorCode ec : ErrorCode.values()) {
            if (ec.code == code) {
                return ec;
            }
        }
        throw new IllegalArgumentException();
    }
}
