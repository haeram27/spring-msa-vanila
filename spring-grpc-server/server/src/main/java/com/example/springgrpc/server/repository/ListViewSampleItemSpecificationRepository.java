package com.example.springgrpc.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.example.springgrpc.server.repository.specification.ListViewSampleSortSpecifications;
import com.example.springgrpc.server.repository.specification.ListViewSampleSpecifications;
import com.example.springgrpc.server.service.dto.ListViewSampleQuery;
import com.example.springgrpc.server.service.dto.ListViewSampleResult;

/**
 * 목록 조회의 영속성 어댑터 중 JPA Criteria({@link Specification}) 구현.
 * {@link ListViewSampleItemQueryRepository}(QueryDSL)와 같은 입출력 계약을 갖는 대체 구현이다.
 * <p>
 * 필터와 정렬을 각각 {@link ListViewSampleSpecifications}, {@link ListViewSampleSortSpecifications}로 만들어
 * {@code and}로 합쳐 한 번의 쿼리로 실행한다. 정렬 {@link Specification}은 조건이 아니라
 * {@code CriteriaQuery.orderBy}를 설정하고 {@code cb.conjunction()}(no-op)을 돌려주는 물건이라
 * 이렇게 합쳐도 필터 조건이 흐려지지 않는다.
 * <p>
 * 정렬이 섞인 {@link Specification}을 그대로 넘겨도 되는 이유 — Spring Data의
 * {@code SimpleJpaRepository#getCountQuery}가 count 쿼리를 만들 때
 * {@code query.orderBy(Collections.emptyList())}로 정렬을 지운다. 정렬 컬럼이 select 목록에 없어
 * count 쿼리가 깨지는 일은 없다.
 * <p>
 * QueryDSL 타입과 JPA 엔티티가 그렇듯 {@link Specification}도 이 패키지 밖으로 나가지 않는다.
 * 서비스는 {@link ListViewSampleQuery}를 주고 {@link ListViewSampleResult}를 받을 뿐이다.
 */
@Repository
public class ListViewSampleItemSpecificationRepository {

    private final ListViewSampleItemRepository listViewItemRepository;

    public ListViewSampleItemSpecificationRepository(ListViewSampleItemRepository listViewItemRepository) {
        this.listViewItemRepository = listViewItemRepository;
    }

    /**
     * 필터/정렬/페이징을 적용해 한 페이지를 읽는다.
     *
     * @param query      페이징과 정렬이 이미 기본값으로 채워진 질의
     * @param pageNumber 1부터 시작
     * @param pageSize   1 이상
     */
    public ListViewSampleResult search(ListViewSampleQuery query, int pageNumber, int pageSize) {
        ListViewSampleQuery.Sort sort = query.sort();
        Specification<ListViewSampleItemEntity> specification = ListViewSampleSpecifications.fromFilter(query.filter())
            .and(ListViewSampleSortSpecifications.orderBy(toSortKey(sort), toSortOrder(sort)));

        // PageRequest 는 0부터 시작하는 페이지 번호를 쓴다. 정렬은 Specification 안에서 이미
        // 설정하므로 Pageable 에는 Sort 를 싣지 않는다.
        Page<ListViewSampleItemEntity> page =
            listViewItemRepository.findAll(specification, PageRequest.of(pageNumber - 1, pageSize));

        return new ListViewSampleResult(
            page.getContent().stream().map(ListViewSampleItemEntity::toDomain).toList(),
            (int) page.getTotalElements());
    }

    /**
     * 도메인 정렬 키를 {@link ListViewSampleSortSpecifications}의 화이트리스트 값으로 옮긴다.
     * <p>
     * {@code name().toLowerCase()} 대신 명시적 switch를 쓴다. 도메인에 키가 추가되면 런타임에
     * "Unknown sort_key value"로 조용히 실패하는 대신 컴파일 에러로 드러난다.
     */
    private static String toSortKey(ListViewSampleQuery.Sort sort) {
        if (sort == null || sort.key() == null) {
            return null;
        }
        return switch (sort.key()) {
            case ID -> "id";
            case NAME -> "name";
            case STATUS -> "status";
            case CATEGORY -> "category";
            case FREQUENCY -> "frequency";
        };
    }

    private static String toSortOrder(ListViewSampleQuery.Sort sort) {
        if (sort == null || sort.order() == null) {
            return null;
        }
        return switch (sort.order()) {
            case ASC -> "ASC";
            case DESC -> "DESC";
        };
    }
}
