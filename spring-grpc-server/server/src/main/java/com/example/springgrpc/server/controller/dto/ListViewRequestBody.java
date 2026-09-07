package com.example.springgrpc.server.controller.dto;

import java.util.List;

import com.example.springgrpc.server.service.dto.ListViewEntry;
import com.example.springgrpc.server.service.dto.ListViewQuery;

/**
 * {@code /api/list-view-sample}의 요청 본문.
 * <p>
 * proto의 {@code FilterNode}는 임의 깊이의 트리지만 이 표면은 조건을 평평하게 받아 하나의 AND로
 * 묶는다. 목록 API에서 실제로 쓰이는 형태가 그쪽이다.
 * <p>
 * 생략된 필드의 기본값은 compact 생성자에서 한 번에 채운다. Jackson이 레코드를 역직렬화할 때도
 * 정규 생성자를 거치므로 여기서 정규화한 값이 그대로 쓰인다.
 */
public record ListViewRequestBody(
    String search,
    List<ListViewEntry.Status> status,
    List<ListViewEntry.Category> category,
    List<ListViewEntry.Frequency> frequency,
    Integer pageSize,
    Integer pageNumber,
    ListViewQuery.Sort.Key sortKey,
    ListViewQuery.Sort.Order sortOrder
) {
    public ListViewRequestBody {
        status = status == null ? List.of() : List.copyOf(status);
        category = category == null ? List.of() : List.copyOf(category);
        frequency = frequency == null ? List.of() : List.copyOf(frequency);
        // 0은 "지정 안 함"이라 서비스가 기본 페이지 크기를 적용한다.
        pageSize = pageSize == null ? 0 : pageSize;
        pageNumber = pageNumber == null ? 0 : pageNumber;
        sortKey = sortKey == null ? ListViewQuery.Sort.Key.ID : sortKey;
        sortOrder = sortOrder == null ? ListViewQuery.Sort.Order.ASC : sortOrder;
    }

    /** 본문이 통째로 생략된 경우에 쓰는 기본 질의. */
    public static ListViewRequestBody empty() {
        return new ListViewRequestBody(null, null, null, null, null, null, null, null);
    }
}
