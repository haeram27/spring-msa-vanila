package com.example.springgrpc.server.repository.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.springgrpc.server.repository.ListViewItemEntity;
import com.example.springgrpc.server.service.dto.ListViewFilter;

/**
 * 도메인 필터 트리({@link ListViewFilter})를 {@code tb_list_view} 조회용 {@link Specification}으로
 * 옮긴다. {@code ListViewItemQueryRepository}가 QueryDSL로 하는 일을 JPA Criteria로 한 것이다.
 *
 * <p>입력은 {@code controller/dto}가 아니라 도메인 타입이다. 저장소 어댑터가 웹 계층 DTO를 알면
 * gRPC 경로에서 같은 조립을 재사용할 수 없다.
 *
 * <p>필터가 {@code null}이거나 {@code values}/{@code targets}가 비어 있으면 "조건 없음"으로 보고
 * 전체를 통과시킨다({@link Specification#unrestricted()}). 필터 트리는 sealed이므로 종류가 늘면
 * 아래 switch가 컴파일 에러로 드러난다.
 */
public final class ListViewSpecifications {

    private ListViewSpecifications() {
    }

    public static Specification<ListViewItemEntity> fromFilter(ListViewFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return switch (filter) {
            case ListViewFilter.And and -> fromAnd(and);
            case ListViewFilter.SearchString searchString -> fromSearchString(searchString);
            case ListViewFilter.StatusIn status -> fromStatus(status);
            case ListViewFilter.CategoryIn category -> fromCategory(category);
            case ListViewFilter.FrequencyIn frequency -> fromFrequency(frequency);
        };
    }

    private static Specification<ListViewItemEntity> fromAnd(ListViewFilter.And and) {
        List<ListViewFilter> targets = and.targets();
        if (targets == null || targets.isEmpty()) {
            return Specification.unrestricted();
        }
        return Specification.allOf(targets.stream().map(ListViewSpecifications::fromFilter).toList());
    }

    /**
     * {@code name}에 대한 부분 일치(대소문자 무시).
     * <p>
     * 검색어에 들어 있는 {@code %}/{@code _}는 LIKE 와일드카드가 아니라 리터럴로 다뤄야 한다.
     * QueryDSL의 {@code containsIgnoreCase}는 이 이스케이프를 알아서 하므로, 두 경로가 같은 결과를
     * 내려면 이쪽도 같은 처리를 해야 한다.
     */
    private static Specification<ListViewItemEntity> fromSearchString(ListViewFilter.SearchString searchString) {
        String value = searchString.value();
        if (value == null || value.isBlank()) {
            return Specification.unrestricted();
        }
        String pattern = "%" + escapeLike(value.toLowerCase()) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern, '\\');
    }

    private static Specification<ListViewItemEntity> fromStatus(ListViewFilter.StatusIn status) {
        if (status.values() == null || status.values().isEmpty()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> root.get("status").in(status.values());
    }

    private static Specification<ListViewItemEntity> fromCategory(ListViewFilter.CategoryIn category) {
        if (category.values() == null || category.values().isEmpty()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> root.get("category").in(category.values());
    }

    private static Specification<ListViewItemEntity> fromFrequency(ListViewFilter.FrequencyIn frequency) {
        if (frequency.values() == null || frequency.values().isEmpty()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> root.get("frequency").in(frequency.values());
    }

    /** 역슬래시를 먼저 바꿔야 뒤에서 새로 넣은 이스케이프 문자를 다시 이스케이프하지 않는다. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
