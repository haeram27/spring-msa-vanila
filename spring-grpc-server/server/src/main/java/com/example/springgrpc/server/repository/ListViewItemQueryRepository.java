package com.example.springgrpc.server.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.springgrpc.server.service.dto.ListViewEntry;
import com.example.springgrpc.server.service.dto.ListViewFilter;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * 목록 조회의 영속성 어댑터. 도메인 질의를 QueryDSL로 옮기고 결과를 다시 도메인으로 돌려준다.
 * <p>
 * QueryDSL과 JPA 엔티티는 이 패키지 밖으로 나가지 않는다. 서비스는 {@link ListViewQuery}를 주고
 * {@link ListViewResult}를 받을 뿐이라, 저장소를 바꿔도 서비스와 어댑터는 그대로다.
 * <p>
 * 필터가 재귀 트리라서 문자열 JPQL로는 조립이 지저분해진다. QueryDSL의
 * {@link BooleanBuilder}는 트리 구조를 그대로 따라가며 조건을 합칠 수 있어 이 모양에 잘 맞는다.
 */
@Repository
public class ListViewItemQueryRepository {

    private static final QListViewItemEntity ITEM = QListViewItemEntity.listViewItemEntity;

    private final JPAQueryFactory queryFactory;

    public ListViewItemQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 필터/정렬/페이징을 적용해 한 페이지를 읽는다.
     *
     * @param query    페이징과 정렬이 이미 기본값으로 채워진 질의
     * @param pageSize 1 이상
     * @param offset   0 이상
     */
    public ListViewResult search(ListViewQuery query, int offset, int pageSize) {
        BooleanBuilder where = new BooleanBuilder(toPredicate(query.filter()));

        List<ListViewEntry> items = queryFactory
            .selectFrom(ITEM)
            .where(where)
            .orderBy(toOrder(query.sort()))
            .offset(offset)
            .limit(pageSize)
            .fetch()
            .stream()
            .map(ListViewItemEntity::toDomain)
            .toList();

        // 페이징 전 전체 건수. count 쿼리를 따로 날려야 하며, null이면 조건에 맞는 행이 없다는 뜻이다.
        Long total = queryFactory
            .select(ITEM.count())
            .from(ITEM)
            .where(where)
            .fetchOne();

        return new ListViewResult(items, total == null ? 0 : total.intValue());
    }

    /**
     * 도메인 필터 트리를 QueryDSL 조건으로 옮긴다. sealed 타입이라 분기 누락은 컴파일 단계에서 걸린다.
     * <p>
     * {@code null}은 "조건 없음"이며 {@link BooleanBuilder}가 그대로 무시한다.
     */
    private static Predicate toPredicate(ListViewFilter filter) {
        if (filter == null) {
            return null;
        }

        return switch (filter) {
            case ListViewFilter.And and -> {
                BooleanBuilder builder = new BooleanBuilder();
                and.targets().forEach(target -> builder.and(toPredicate(target)));
                // 하위 조건이 하나도 없으면 전체를 걸러내지 않도록 null을 돌려준다.
                // getValue()의 반환 타입은 Predicate 다. BooleanExpression 으로 캐스팅하면
                // 조건이 둘 이상일 때 PredicateOperation 이 나와 ClassCastException 이 난다.
                yield builder.getValue();
            }
            case ListViewFilter.SearchString search ->
                search.value() == null || search.value().isBlank()
                    ? null
                    : ITEM.name.containsIgnoreCase(search.value());
            case ListViewFilter.StatusIn status ->
                status.values().isEmpty() ? null : ITEM.status.in(status.values());
            case ListViewFilter.CategoryIn category ->
                category.values().isEmpty() ? null : ITEM.category.in(category.values());
            case ListViewFilter.FrequencyIn frequency ->
                frequency.values().isEmpty() ? null : ITEM.frequency.in(frequency.values());
        };
    }

    /**
     * 정렬 키를 컬럼으로 옮긴다. 같은 값이 여러 건일 때 페이지 경계에서 순서가 흔들리지 않도록
     * 항상 id를 뒤에 덧붙인다.
     */
    private static OrderSpecifier<?>[] toOrder(ListViewQuery.Sort sort) {
        boolean asc = sort == null || sort.order() != ListViewQuery.Sort.Order.DESC;
        ListViewQuery.Sort.Key key = sort == null ? ListViewQuery.Sort.Key.ID : sort.key();

        OrderSpecifier<?> tiebreaker = asc ? ITEM.id.asc() : ITEM.id.desc();
        if (key == ListViewQuery.Sort.Key.ID) {
            // 이미 id 로 정렬하므로 같은 컬럼을 두 번 넣지 않는다.
            return new OrderSpecifier<?>[] { tiebreaker };
        }

        OrderSpecifier<?> primary = switch (key) {
            case ID -> tiebreaker;
            case NAME -> asc ? ITEM.name.asc() : ITEM.name.desc();
            case STATUS -> asc ? ITEM.status.asc() : ITEM.status.desc();
            case CATEGORY -> asc ? ITEM.category.asc() : ITEM.category.desc();
            case FREQUENCY -> asc ? ITEM.frequency.asc() : ITEM.frequency.desc();
        };

        return new OrderSpecifier<?>[] { primary, tiebreaker };
    }
}
