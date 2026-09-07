package com.example.springgrpc.server.service.dto;

/**
 * 목록 한 건을 표현하는 Service 계층의 도메인 타입. proto 타입에 의존하지 않는다.
 * <p>
 * 중첩 enum들은 proto의 동명 enum과 상수 이름이 같지만 {@code *_UNSPECIFIED}는 두지 않는다.
 * 그건 proto3가 0번 값을 요구해서 생긴 wire 사정이지 도메인 상태가 아니며,
 * 여기서 값이 없다는 것은 {@code null}이나 필터 미적용으로 표현한다.
 */
public record ListViewEntry(
    String id,
    String name,
    Status status,
    Category category,
    Frequency frequency
) {
    public enum Status {
        PENDING, CREATING, CREATED, FAILED, DELETED
    }

    public enum Category {
        BASIC, UNIFIED, QUERY
    }

    public enum Frequency {
        IMMEDIATE, SPECIFIC_TIME, EVERY_DAY, EVERY_WEEK, EVERY_MONTH_DAY, EVERY_YEAR
    }
}
