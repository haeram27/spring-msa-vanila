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
import com.example.springgrpc.server.service.dto.ListViewEntry;
import com.example.springgrpc.server.service.dto.ListViewFilter;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;

import jakarta.persistence.EntityManager;

/**
 * QueryDSL 필터/정렬/페이징을 인메모리 H2 에 대고 검증한다.
 * <p>
 * {@code @DataJpaTest}(Boot 4 부터 {@code spring-boot-starter-data-jpa-test} 에 있다)는 JPA 계층만 올리므로 {@link QuerydslConfiguration} 과 리포지토리는
 * 직접 import 한다. gRPC 서버나 웹 계층은 뜨지 않아 테스트가 빠르고 포트도 잡지 않는다.
 */
@DataJpaTest
@Import({ QuerydslConfiguration.class, ListViewItemQueryRepository.class })
class ListViewItemQueryRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ListViewItemQueryRepository repository;

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

    @Test
    @DisplayName("필터가 없으면 전체를 id 오름차순으로 돌려준다")
    void listsAllWhenNoFilter() {
        ListViewResult result = repository.search(query(null, null), 0, 20);

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.items()).extracting(ListViewEntry::id)
            .containsExactly("rpt-001", "rpt-002", "rpt-004", "rpt-007");
    }

    @Test
    @DisplayName("이름 부분 일치는 대소문자를 구분하지 않는다")
    void searchStringIgnoresCase() {
        ListViewResult result = repository.search(
            query(new ListViewFilter.SearchString("TRAFFIC"), null), 0, 20);

        assertThat(result.items()).extracting(ListViewEntry::id)
            .containsExactly("rpt-001", "rpt-004", "rpt-007");
    }

    @Test
    @DisplayName("중첩 AND 트리의 모든 조건이 함께 적용된다")
    void appliesNestedAndTree() {
        ListViewFilter filter = new ListViewFilter.And(List.of(
            new ListViewFilter.SearchString("traffic"),
            new ListViewFilter.And(List.of(
                new ListViewFilter.StatusIn(Set.of(ListViewEntry.Status.CREATED, ListViewEntry.Status.DELETED)),
                new ListViewFilter.CategoryIn(Set.of(ListViewEntry.Category.BASIC, ListViewEntry.Category.QUERY))
            ))
        ));

        ListViewResult result = repository.search(query(filter, null), 0, 20);

        assertThat(result.items()).extracting(ListViewEntry::id)
            .containsExactly("rpt-001", "rpt-007");
    }

    @Test
    @DisplayName("비어 있는 AND 는 아무것도 거르지 않는다")
    void emptyAndMatchesEverything() {
        ListViewResult result = repository.search(
            query(new ListViewFilter.And(List.of()), null), 0, 20);

        assertThat(result.totalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("이름 내림차순으로 정렬한다")
    void sortsByNameDescending() {
        ListViewResult result = repository.search(
            query(null, new ListViewQuery.Sort(ListViewQuery.Sort.Key.NAME, ListViewQuery.Sort.Order.DESC)),
            0, 20);

        assertThat(result.items()).extracting(ListViewEntry::name)
            .containsExactly("Weekly Threat Summary", "Legacy Traffic Archive",
                "Daily Traffic Report", "Ad-hoc Traffic Query");
    }

    @Test
    @DisplayName("totalCount 는 페이징 이전 건수다")
    void totalCountIgnoresPaging() {
        ListViewResult result = repository.search(query(null, null), 2, 2);

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.items()).extracting(ListViewEntry::id).containsExactly("rpt-004", "rpt-007");
    }

    @Test
    @DisplayName("범위를 벗어난 페이지는 빈 목록을 돌려준다")
    void returnsEmptyPageBeyondRange() {
        ListViewResult result = repository.search(query(null, null), 100, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isEqualTo(4);
    }
}
