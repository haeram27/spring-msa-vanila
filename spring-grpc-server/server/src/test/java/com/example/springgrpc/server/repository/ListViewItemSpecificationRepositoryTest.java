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

import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.Status;
import com.example.springgrpc.server.service.dto.ListViewSampleEntry;
import com.example.springgrpc.server.service.dto.ListViewSampleFilter;
import com.example.springgrpc.server.service.dto.ListViewSampleQuery;
import com.example.springgrpc.server.service.dto.ListViewSampleResult;

import jakarta.persistence.EntityManager;

/**
 * Specification 필터/정렬/페이징을 인메모리 H2 에 대고 검증한다.
 * <p>
 * {@link ListViewItemQueryRepositoryTest}(QueryDSL)와 같은 시드/기대값을 쓴다. 두 어댑터가 같은
 * 입력에 같은 결과를 내야 한다는 것이 이 샘플의 요점이라, 한쪽만 고치면 여기서 어긋난다.
 */
@DataJpaTest
@Import(ListViewSampleItemSpecificationRepository.class)
class ListViewItemSpecificationRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ListViewSampleItemSpecificationRepository repository;

    private Long dailyId;
    private Long weeklyId;
    private Long adhocId;
    private Long legacyId;

    @BeforeEach
    void seed() {
        ListViewSampleItemEntity daily = new ListViewSampleItemEntity("Daily Traffic Report",
            Status.CREATED, Category.BASIC, Frequency.EVERY_DAY);
        ListViewSampleItemEntity weekly = new ListViewSampleItemEntity("Weekly Threat Summary",
            Status.CREATED, Category.UNIFIED, Frequency.EVERY_WEEK);
        ListViewSampleItemEntity adhoc = new ListViewSampleItemEntity("Ad-hoc Traffic Query",
            Status.CREATING, Category.QUERY, Frequency.IMMEDIATE);
        ListViewSampleItemEntity legacy = new ListViewSampleItemEntity("Legacy Traffic Archive",
            Status.DELETED, Category.QUERY, Frequency.EVERY_DAY);

        List.of(daily, weekly, adhoc, legacy).forEach(entityManager::persist);
        entityManager.flush();

        dailyId = daily.getId();
        weeklyId = weekly.getId();
        adhocId = adhoc.getId();
        legacyId = legacy.getId();
    }

    private static ListViewSampleQuery query(ListViewSampleFilter filter, ListViewSampleQuery.Sort sort) {
        return new ListViewSampleQuery(filter, null, sort);
    }

    private static List<Long> idsOf(ListViewSampleResult result) {
        return result.items().stream().map(ListViewSampleEntry::id).toList();
    }

    @Test
    @DisplayName("필터가 없으면 전체를 id 오름차순으로 돌려준다")
    void listsAllWhenNoFilter() {
        ListViewSampleResult result = repository.search(query(null, null), 1, 20);

        assertThat(idsOf(result)).containsExactly(dailyId, weeklyId, adhocId, legacyId);
        assertThat(result.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("검색어는 name 에 대소문자 구분 없이 부분 일치한다")
    void filtersBySearchStringIgnoringCase() {
        ListViewSampleResult result = repository.search(
            query(new ListViewSampleFilter.SearchString("TRAFFIC"), null), 1, 20);

        assertThat(idsOf(result)).containsExactly(dailyId, adhocId, legacyId);
    }

    @Test
    @DisplayName("검색어의 % 는 와일드카드가 아니라 리터럴로 다룬다")
    void escapesLikeWildcardsInSearchString() {
        ListViewSampleResult result = repository.search(
            query(new ListViewSampleFilter.SearchString("%"), null), 1, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isZero();
    }

    @Test
    @DisplayName("중첩 AND 트리의 조건을 모두 만족하는 항목만 남긴다")
    void appliesNestedAndTree() {
        ListViewSampleFilter filter = new ListViewSampleFilter.And(List.of(
            new ListViewSampleFilter.SearchString("traffic"),
            new ListViewSampleFilter.And(List.of(
            new ListViewSampleFilter.StatusIn(Set.of(Status.CREATED, Status.DELETED)),
            new ListViewSampleFilter.CategoryIn(Set.of(Category.QUERY))))));

        ListViewSampleResult result = repository.search(query(filter, null), 1, 20);

        assertThat(idsOf(result)).containsExactly(legacyId);
    }

    @Test
    @DisplayName("정렬 키가 같은 값이면 id 타이브레이커로 순서가 고정된다")
    void appendsIdTiebreaker() {
        ListViewSampleQuery.Sort sort =
            new ListViewSampleQuery.Sort(ListViewSampleQuery.Sort.Key.FREQUENCY, ListViewSampleQuery.Sort.Order.ASC);

        ListViewSampleResult result = repository.search(
            query(new ListViewSampleFilter.FrequencyIn(Set.of(Frequency.EVERY_DAY)), sort), 1, 20);

        assertThat(idsOf(result)).containsExactly(dailyId, legacyId);
    }

    @Test
    @DisplayName("정렬 내림차순은 지정한 컬럼과 타이브레이커에 모두 적용된다")
    void sortsDescending() {
        ListViewSampleQuery.Sort sort =
            new ListViewSampleQuery.Sort(ListViewSampleQuery.Sort.Key.NAME, ListViewSampleQuery.Sort.Order.DESC);

        ListViewSampleResult result = repository.search(query(null, sort), 1, 20);

        assertThat(idsOf(result)).containsExactly(weeklyId, legacyId, dailyId, adhocId);
    }

    @Test
    @DisplayName("totalCount 는 페이징 전 전체 건수다")
    void countsBeforePaging() {
        ListViewSampleResult result = repository.search(query(null, null), 2, 3);

        assertThat(idsOf(result)).containsExactly(legacyId);
        assertThat(result.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("허용되지 않은 정렬 키는 쿼리 실행 전에 거부된다")
    void rejectsUnknownSortKey() {
        assertThatThrownBy(() -> com.example.springgrpc.server.repository.specification.ListViewSampleSortSpecifications
            .orderBy("password", "ASC"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown sort_key value");
    }
}
