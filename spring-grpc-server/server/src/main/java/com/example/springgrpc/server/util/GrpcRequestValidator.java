package com.example.springgrpc.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC 어댑터가 proto 요청 필드를 검증할 때 쓰는 공용 유틸.
 * <p>
 * proto3는 스칼라 필드에 기본값({@code ""}, {@code 0})을 채워 넣으므로 "값이 오지 않았다"와
 * "빈 값이 왔다"를 구분할 수 없다. 따라서 필수 필드는 어댑터 진입 시점에 직접 확인해야 한다.
 * <p>
 * 검증 실패는 {@link IllegalArgumentException}으로 던지며, 어댑터의 {@code execute(...)}가
 * 이를 {@code Status.INVALID_ARGUMENT}로 변환한다.
 */
public final class GrpcRequestValidator {

    private static final Logger log = LoggerFactory.getLogger(GrpcRequestValidator.class);

    private GrpcRequestValidator() {
    }

    /**
     * 문자열 필드가 비어 있지 않은지 확인한다.
     *
     * @param value     검사 대상 값
     * @param fieldName 오류 메시지에 노출할 proto 필드명
     * @throws IllegalArgumentException 값이 {@code null}이거나 공백뿐인 경우
     */
    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            log.error("Validation failed: {} is blank", fieldName);
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
