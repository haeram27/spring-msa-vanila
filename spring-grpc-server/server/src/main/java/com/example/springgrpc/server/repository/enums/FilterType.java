package com.example.springgrpc.server.repository.enums;

/**
 * 필터 트리 노드의 판별자. Jackson이 {@code type} 프로퍼티로 하위 타입을 고르는 데 쓴다.
 * <p>
 * 상수 이름이 곧 JSON에 실리는 값이므로 이름 변경은 API 호환을 깬다.
 */
public enum FilterType {
    AND,
    SEARCH_STRING,
    STATUS,
    CATEGORY,
    FREQUENCY
}
