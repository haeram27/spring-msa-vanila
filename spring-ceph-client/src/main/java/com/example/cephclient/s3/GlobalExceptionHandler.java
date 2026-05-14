package com.example.cephclient.s3;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String code, String message) {}

    @ExceptionHandler(BulkPresignException.class)
    public ResponseEntity<ErrorResponse> handleBulkPresignException(BulkPresignException e) {
        return switch (e.getReason()) {
            case TIMEOUT -> {
                log.warn("bulk presign timeout: {}", e.getMessage(), e);
                yield ResponseEntity
                    .status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(new ErrorResponse("BULK_PRESIGN_TIMEOUT", "Bulk presign timed out"));
            }
            case PRESIGN_FAILURE -> {
                log.error("bulk presign failure: {}", e.getMessage(), e);
                yield ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("BULK_PRESIGN_FAILURE", "Bulk presign failed"));
            }
        };
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("invalid request: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));
    }
}
