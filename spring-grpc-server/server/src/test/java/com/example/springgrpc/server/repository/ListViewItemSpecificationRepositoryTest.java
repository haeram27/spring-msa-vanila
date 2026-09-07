package com.example.springgrpc.server.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.springgrpc.server.service.dto.ListViewEntry;
import com.example.springgrpc.server.service.dto.ListViewFilter;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;

import jakarta.persistence.EntityManager;

/**
 * Specification 필터/정렬/페이징을 인메모리 H2 에 대고 검증한다.
 * <p>
 * {@link ListViewItemQueryRepositoryTest}(QueryDSL)와 같은 시드/기대값을 쓴다. 두 어댑터가 같은
 * 입력에 같은 결과를 내야 한다는 것이 이 샘플의 요점이라, 한쪽만 고치면 여기서 어긋난다.
 */
@DataJpaTest
@Import(ListViewItemSpecificationRepository.class)
class ListViewItemSpecificationRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ListViewItemSpecificationRepository repository;

    @BeforeEach
    void seed() {
        List.of(
            new ListViewItemEntity("rpt-001", "Daily Traffic Report",
                ListViewEntry.Status.CREATED, ListViewEntry.Category.BASIC, ListViewEntry.Frequency.EVERY_DAY),
            new ListViewItemEntity("rpt-002", "Weekly Threat Summary",
                ListViewEntry.Status.CREATED, ListViewEntry.Category.UNIFIED, ListViewEntry.Frequency.EVERY_WEEK),
            new ListViewItemEntity("rpt-004", "Ad-hoc Traffic Query",
                ListViewEntry.Status.CREATING, ListViewEntry.Category.QUERY, ListViewEntry.Frequency.IMMEDIATE),
            new ListViewItemEntity("rpt-007", "Legacy Traffic Archive",
                ListViewEntry.Status.DELETED, ListViewEntry.Category.QUERY, ListViewEntry.Frequency.EVERY_DAY)
        ).forEach(entityManager::persist);
        entityManager.flush();
    }

    private static ListViewQuery query(ListViewFilter filter, ListViewQuery.Sort sort) {
        return new ListViewQuery(filter, null, sort);
    }

    private static List<String> idsOf(ListViewResult result) {
        return result.items().stream().map(ListViewEntry::id).toList();
    }

    @Test
    @DisplayName("필터가 없으면 전체를 id 오름차순으로 돌려준다")
    void listsAllWhenNoFilter() {
        ListViewResult result = repository.search(query(null, null), 1, 20);

        assertThat(idsOf(result)).containsExactly("rpt-001", "rpt-002", "rpt-004", "rpt-007");
        assertThat(result.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("검색어는 name 에 대소문자 구분 없이 부분 일치한다")
    void filtersBySearchStringIgnoringCase() {
        ListViewResult result = repository.search(
            query(new ListViewFilter.SearchString("TRAFFIC"), null), 1, 20);

        assertThat(idsOf(result)).containsExactly("rpt-001", "rpt-004", "rpt-007");
    }

    @Test
    @DisplayName("검색어의 % 는 와일드카드가 아니라 리터럴로 다룬다")
    void escapesLikeWildcardsInSearchString() {
        ListViewResult result = repository.search(
            query(new ListViewFilter.SearchString("%"), null), 1, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    @Test
    @DisplayName("중첩 AND 트리의 조건을 모두 만족하는 항목만 남긴다")
    void appliesNestedAndTree() {
        ListViewFilter filter = new ListViewFilter.And(List.of(
            new ListViewFilter.SearchString("traffic"),
            new ListViewFilter.And(List.of(
                new ListViewFilter.StatusIn(Set.of(ListViewEntry.Status.CREATED, ListViewEntry.Status.DELETED)),
                new ListViewFilter.CategoryIn(Set.of(ListViewEntry.Category.QUERY))))));

        ListViewResult result = repository.search(query(filter, null), 1, 20);

        assertThat(idsOf(result)).containsExactly("rpt-007");
    }

    @Test
    @DisplayName("정렬 키가 같은 값이면 id 타이브레이커로 순서가 고정된다")
    void appendsIdTiebreaker() {
        ListViewQuery.Sort sort =
            new ListViewQuery.Sort(ListViewQuery.Sort.Key.FREQUENCY, ListViewQuery.Sort.Order.ASC);

        ListViewResult result = repository.search(
            query(new ListViewFilter.FrequencyIn(Set.of(ListViewEntry.Frequency.EVERY_DAY)), sort), 1, 20);

        assertThat(idsOf(result)).containsExactly("rpt-001", "rpt-007");
    }

    @Test
    @DisplayName("정렬 내림차순은 지정한 컬럼과 타이브레이커에 모두 적용된다")
    void sortsDescending() {
        ListViewQuery.Sort sort =
            new ListViewQuery.Sort(ListViewQuery.Sort.Key.NAME, ListViewQuery.Sort.Order.DESC);

        ListViewResult result = repository.search(query(null, sort), 1, 20);

        assertThat(idsOf(result)).containsExactly("rpt-002", "rpt-007", "rpt-001", "rpt-004");
    }

    @Test
    @DisplayName("totalCount 는 페이징 전 전체 건수다")
    void countsBeforePaging() {
        ListViewResult result = repository.search(query(null, null), 2, 3);

        assertThat(idsOf(result)).containsExactly("rpt-007");
        assertThat(result.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("허용되지 않은 정렬 키는 쿼리 실행 전에 거부된다")
    void rejectsUnknownSortKey() {
        assertThatThrownBy(() -> com.example.springgrpc.server.repository.specification.ListViewSortSpecifications
            .orderBy("password", "ASC"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown sort_key value");
    }
}
