package com.example.springgrpc.server.util;

import java.util.List;
import java.util.Locale;

/**
 * 자유 문자열로 들어온 요청 필드를 도메인 enum으로 좁힌다.
 * <p>
 * 접두사가 없는 것은 REST와 gRPC 두 스택이 함께 쓴다는 뜻이다. 같은 잘못된 값에 두 경로가 서로
 * 다른 오류를 내면 "두 경로가 같은 응답을 낸다"는 이 샘플의 전제가 깨지므로 한곳에 둔다.
 * <p>
 * 오류 메시지에는 요청 필드명과 enum 상수만 담는다. 클래스명이나 패키지는 싣지 않는다.
 */
public final class EnumParser {

    private EnumParser() {
    }

    /**
     * @param value        비었거나 {@code null}이면 {@code defaultValue}를 쓴다
     * @param fieldName    오류 메시지에 실을 요청 필드 이름(wire 이름)
     * @throws IllegalArgumentException 상수에 없는 값. REST에서는 어드바이스가 HTTP 400으로,
     *                                  gRPC에서는 {@code GrpcCallExecutor}가
     *                                  {@code INVALID_ARGUMENT}로 옮긴다
     */
    public static <E extends Enum<E>> E parse(String value, String fieldName, Class<E> type, E defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "%s must be one of %s but was '%s'".formatted(fieldName, List.of(type.getEnumConstants()), value), e);
        }
    }
}
