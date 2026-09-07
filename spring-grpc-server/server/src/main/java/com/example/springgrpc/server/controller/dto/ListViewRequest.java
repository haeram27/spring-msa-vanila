package com.example.springgrpc.server.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ListViewRequest(
    ListViewFilter filter,
    Pagination pagination,
    Sort sort               // null 허용 → "정렬 안 함/기본 정렬"
) {

    /**
     * 본문 자체가 생략된 요청({@code @RequestBody(required = false)})의 기본값.
     * <p>
     * 세 필드 모두 {@code null}은 "지정 안 함"이고, 실제 기본값(페이지 크기, 정렬)은 서비스가 정한다.
     */
    public static ListViewRequest empty() {
        return new ListViewRequest(null, null, null);
    }
}