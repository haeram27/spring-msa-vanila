package com.example.springwebex.exception.ecode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorCode {
// @formatter:off
    SUCCESS(0, "success"),
    UNKNOWN(1, "unknown error");
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
