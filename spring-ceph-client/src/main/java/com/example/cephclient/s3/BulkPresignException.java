package com.example.cephclient.s3;

public class BulkPresignException extends RuntimeException {

    public enum Reason {
        PRESIGN_FAILURE,
        TIMEOUT
    }

    private final Reason reason;

    public BulkPresignException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
