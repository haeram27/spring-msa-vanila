package com.example.springgrpc.server.service.dto;

import java.util.List;

/**
 * 목록 조회 결과. proto {@code ListViewResponse}의 도메인 대응물이다.
 *
 * @param items      현재 페이지에 해당하는 항목
 * @param totalCount 필터 적용 후 전체 건수. 페이징 전 값이라 {@code items.size()}와 다를 수 있다
 */
public record ListViewResult(List<ListViewEntry> items, int totalCount) {
    public ListViewResult {
        items = List.copyOf(items);
    }
}
