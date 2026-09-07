package com.example.springgrpc.server.repository.enums;

/**
 * 목록 항목의 상태.
 * <p>
 * proto의 동명 enum과 상수 이름은 같지만 {@code *_UNSPECIFIED}는 두지 않는다. 그건 proto3가
 * 0번 값을 요구해서 생긴 wire 사정이라 이쪽 상태가 아니며, 값이 없다는 것은 {@code null}이나
 * 필터 미적용으로 표현한다.
 */
public enum Status {
    PENDING,
    CREATING,
    CREATED,
    FAILED,
    DELETED
}
