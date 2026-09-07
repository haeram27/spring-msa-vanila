package com.example.springgrpc.server.repository.enums;

/**
 * 목록 항목의 분류.
 * <p>
 * {@link Status}와 같은 이유로 {@code *_UNSPECIFIED}를 두지 않는다.
 */
public enum Category {
    BASIC,
    UNIFIED,
    QUERY
}
