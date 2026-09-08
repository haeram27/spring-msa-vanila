package com.example.springgrpc.server.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.example.springgrpc.server.repository.ListViewSampleItemEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

/**
 * {@code tb_list_view_sample} 조회 정렬용 {@link Specification}을 생성한다.
 *
 * <p>정렬 대상 ({@code id}/{@code name}/{@code status}/
 * {@code category}/{@code frequency}/{@code modified_at}/{@code created_at}) 를 {@link Specification}에서
 * {@code CriteriaQuery.orderBy(...)}를 직접 설정하고 조건 자체는 {@code cb.conjunction()}(no-op)을
 * 반환해, 필터 {@link Specification}과 {@code .and()}로 결합해 하나의 쿼리로 실행한다.
 *
 * <p>{@code sort_key}/{@code sort_order} 화이트리스트 검증은 {@link #orderBy}에서 즉시(eager) 수행한다 —
 * {@link Specification}의 {@code toPredicate}(DB 접근 시점)까지 미루지 않아, 잘못된 요청을 쿼리 실행 전에
 * {@link IllegalArgumentException}으로 빠르게 실패시킨다.
 */
public final class ListViewSampleSortSpecifications {

    private ListViewSampleSortSpecifications() {
    }

    /** 허용된 {@code sort_key} 화이트리스트. 그 외 값은 {@link #orderBy}에서 즉시 거부된다. */
    private enum SortKey {
        ID("id"),
        NAME("name"),
        STATUS("status"),
        CATEGORY("category"),
        FREQUENCY("frequency"),
        MODIFIED_AT("modified_at"),
        CREATED_AT("created_at");

        private final String wireValue;

        SortKey(String wireValue) {
            this.wireValue = wireValue;
        }

        static SortKey fromWireValue(String value) {
            for (SortKey key : values()) {
                if (key.wireValue.equals(value)) {
                    return key;
                }
            }
            throw new IllegalArgumentException("Unknown sort_key value: " + value);
        }
    }

    public static Specification<ListViewSampleItemEntity> orderBy(String sortKey, String sortOrder) {
        boolean ascending = toAscending(sortOrder);
        // sort_key 가 없으면 id 오름차순이 도메인 기본값이다(ListViewSampleQuery 의 계약).
        SortKey key = sortKey == null || sortKey.isBlank() ? SortKey.ID : SortKey.fromWireValue(sortKey);
        return (root, query, cb) -> {
            // 같은 값이 여러 건일 때 페이지 경계에서 순서가 흔들리지 않도록 항상 id 를 뒤에 덧붙인다.
            // CriteriaQuery.orderBy 는 목록을 통째로 교체하므로 정렬 두 개를 한 번에 넘겨야 한다.
            Order tiebreaker = ascending ? cb.asc(root.get("id")) : cb.desc(root.get("id"));
            if (key == SortKey.ID) {
                query.orderBy(tiebreaker);
            } else {
                Expression<?> sortExpression = toSortExpression(key, root, cb);
                query.orderBy(ascending ? cb.asc(sortExpression) : cb.desc(sortExpression), tiebreaker);
            }
            return cb.conjunction();
        };
    }

    private static Expression<?> toSortExpression(SortKey key, Root<ListViewSampleItemEntity> root, CriteriaBuilder cb) {
        return switch (key) {
            case ID -> root.get("id");
            case NAME -> root.get("name");
            case STATUS -> root.get("status");
            case CATEGORY -> root.get("category");
            case FREQUENCY -> root.get("frequency");
            case MODIFIED_AT -> root.get("modifiedAt");
            case CREATED_AT -> root.get("createdAt");
        };
    }

    private static boolean toAscending(String sortOrder) {
        if (sortOrder == null || sortOrder.isBlank()) {
            return true;
        }
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            return true;
        }
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            return false;
        }
        throw new IllegalArgumentException("Unknown sort_order value: " + sortOrder);
    }
}