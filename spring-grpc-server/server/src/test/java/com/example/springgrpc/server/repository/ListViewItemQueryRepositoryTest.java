package com.example.springgrpc.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.springgrpc.server.conf.QuerydslConfiguration;
import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.Status;
import com.example.springgrpc.server.service.dto.ListViewSampleEntry;
import com.example.springgrpc.server.service.dto.ListViewSampleFilter;
import com.example.springgrpc.server.service.dto.ListViewSampleQuery;
import com.example.springgrpc.server.service.dto.ListViewSampleResult;

import jakarta.persistence.EntityManager;

/**
 * QueryDSL 필터/정렬/페이징을 인메모리 H2 에 대고 검증한다.
 * <p>
 * {@code @DataJpaTest}(Boot 4 부터 {@code spring-boot-starter-data-jpa-test} 에 있다)는 JPA 계층만 올리므로 {@link QuerydslConfiguration} 과 리포지토리는
 * 직접 import 한다. gRPC 서버나 웹 계층은 뜨지 않아 테스트가 빠르고 포트도 잡지 않는다.
 */
@DataJpaTest
@Import({ QuerydslConfiguration.class, ListViewSampleItemQueryRepository.class })
class ListViewItemQueryRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ListViewSampleItemQueryRepository repository;

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

    @Test
    @DisplayName("필터가 없으면 전체를 id 오름차순으로 돌려준다")
    void listsAllWhenNoFilter() {
        ListViewSampleResult result = repository.search(query(null, null), 0, 20);

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.items()).extracting(ListViewSampleEntry::id)
            .containsExactly(dailyId, weeklyId, adhocId, legacyId);
    }

    @Test
    @DisplayName("이름 부분 일치는 대소문자를 구분하지 않는다")
    void searchStringIgnoresCase() {
        ListViewSampleResult result = repository.search(
            query(new ListViewSampleFilter.SearchString("TRAFFIC"), null), 0, 20);

        assertThat(result.items()).extracting(ListViewSampleEntry::id)
            .containsExactly(dailyId, adhocId, legacyId);
    }

    @Test
    @DisplayName("중첩 AND 트리의 모든 조건이 함께 적용된다")
    void appliesNestedAndTree() {
        ListViewSampleFilter filter = new ListViewSampleFilter.And(List.of(
            new ListViewSampleFilter.SearchString("traffic"),
            new ListViewSampleFilter.And(List.of(
                new ListViewSampleFilter.StatusIn(Set.of(Status.CREATED, Status.DELETED)),
                new ListViewSampleFilter.CategoryIn(Set.of(Category.BASIC, Category.QUERY))
            ))
        ));

        ListViewSampleResult result = repository.search(query(filter, null), 0, 20);

        assertThat(result.items()).extracting(ListViewSampleEntry::id)
            .containsExactly(dailyId, legacyId);
    }

    @Test
    @DisplayName("비어 있는 AND 는 아무것도 거르지 않는다")
    void emptyAndMatchesEverything() {
        ListViewSampleResult result = repository.search(
            query(new ListViewSampleFilter.And(List.of()), null), 0, 20);

        assertThat(result.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("이름 내림차순으로 정렬한다")
    void sortsByNameDescending() {
        ListViewSampleResult result = repository.search(
            query(null, new ListViewSampleQuery.Sort(ListViewSampleQuery.Sort.Key.NAME, ListViewSampleQuery.Sort.Order.DESC)),
            0, 20);

        assertThat(result.items()).extracting(ListViewSampleEntry::name)
            .containsExactly("Weekly Threat Summary", "Legacy Traffic Archive",
                "Daily Traffic Report", "Ad-hoc Traffic Query");
    }

    @Test
    @DisplayName("totalCount 는 페이징 이전 건수다")
    void totalCountIgnoresPaging() {
        ListViewSampleResult result = repository.search(query(null, null), 2, 2);

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.items()).extracting(ListViewSampleEntry::id).containsExactly(adhocId, legacyId);
    }

    @Test
    @DisplayName("범위를 벗어난 페이지는 빈 목록을 돌려준다")
    void returnsEmptyPageBeyondRange() {
        ListViewSampleResult result = repository.search(query(null, null), 100, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isEqualTo(4);
    }
}
