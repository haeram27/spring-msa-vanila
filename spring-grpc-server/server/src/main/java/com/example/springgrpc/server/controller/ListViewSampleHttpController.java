package com.example.springgrpc.server.controller;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springgrpc.server.controller.dto.ListViewFilter;
import com.example.springgrpc.server.controller.dto.ListViewRequest;
import com.example.springgrpc.server.controller.dto.Pagination;
import com.example.springgrpc.server.controller.dto.Sort;
import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.Status;
import com.example.springgrpc.server.service.ListViewSampleService;
import com.example.springgrpc.server.service.dto.ListViewEntry;
import com.example.springgrpc.server.service.dto.ListViewFilter.And;
import com.example.springgrpc.server.service.dto.ListViewFilter.CategoryIn;
import com.example.springgrpc.server.service.dto.ListViewFilter.FrequencyIn;
import com.example.springgrpc.server.service.dto.ListViewFilter.SearchString;
import com.example.springgrpc.server.service.dto.ListViewFilter.StatusIn;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;
import com.example.springgrpc.server.util.EnumParser;
import com.example.springgrpc.server.util.HttpRequestLogger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST 어댑터: 같은 프로세스의 {@link ListViewSampleService}를 직접 호출한다.
 * {@code ListViewSampleGrpcServerService}와 동일한 서비스를 공유하므로 두 경로는 같은 JSON을 낸다.
 * <p>
 * 어댑터의 역할은 본문 DTO ↔ 도메인 매핑과 검증뿐이고 조회 로직은 서비스에 맡긴다. gRPC 어댑터가
 * proto enum을 도메인 enum으로 옮기는 것과 같은 자리에, 이쪽은 {@code repository.enums}의 요청용
 * enum을 옮긴다.
 * <p>
 * enum 매핑을 {@code valueOf(name())} 대신 명시적 switch로 쓴 이유는, 어느 한쪽에 상수가 추가되면
 * 런타임에 조용히 누락되는 대신 이 파일이 컴파일 에러로 먼저 드러나게 하기 위해서다.
 *
 * <pre>
 * curl -X POST 'localhost:8080/api/list-view-sample' -H 'Content-Type: application/json' -d '{
 *   "filter": {"type": "AND", "targets": [
 *     {"type": "SEARCH_STRING", "value": "traffic"},
 *     {"type": "STATUS", "value": ["CREATED", "DELETED"]}
 *   ]},
 *   "pagination": {"page_size": 10, "page_number": 1},
 *   "sort": {"sort_key": "NAME", "sort_order": "DESC"}
 * }'
 * </pre>
 */
@RestController
@RequestMapping("/api/list-view-sample")
public class ListViewSampleHttpController {

    private final ListViewSampleService listViewSampleService;

    public ListViewSampleHttpController(ListViewSampleService listViewSampleService) {
        this.listViewSampleService = listViewSampleService;
    }

    /** enum 필드는 상수 이름 그대로(대문자) 받는다. 값이 틀리면 Spring이 400으로 응답한다. */
    @PostMapping
    public ListViewResult list(@RequestBody(required = false) ListViewRequest body,
                               HttpServletRequest request) {
        HttpRequestLogger.logRequestMembers("ListViewSampleHttpController#list", request);

        ListViewRequest effective = body == null ? ListViewRequest.empty() : body;

        ListViewQuery query = new ListViewQuery(
            toFilter(effective.filter()),
            toPagination(effective.pagination()),
            toSort(effective.sort()));
        return listViewSampleService.list(query);
    }

    // ------------------------------------------------------------------
    // 요청 본문 → 도메인
    // ------------------------------------------------------------------

    /**
     * 필터 트리를 도메인 타입으로 옮긴다. {@code null}은 "조건 없음"이다.
     * <p>
     * 양쪽 모두 sealed 이라 종류가 늘면 이 switch 가 컴파일 에러로 드러난다.
     */
    private static com.example.springgrpc.server.service.dto.ListViewFilter toFilter(ListViewFilter node) {
        if (node == null) {
            return null;
        }
        return switch (node) {
            case ListViewFilter.AndFilter and -> new And(toTargets(and.targets()));
            case ListViewFilter.SearchStringFilter searchString -> new SearchString(searchString.value());
            case ListViewFilter.StatusFilter status ->
                new StatusIn(toDomainSet(status.value(), ListViewSampleHttpController::toDomain));
            case ListViewFilter.CategoryFilter category ->
                new CategoryIn(toDomainSet(category.value(), ListViewSampleHttpController::toDomain));
            case ListViewFilter.FrequencyFilter frequency ->
                new FrequencyIn(toDomainSet(frequency.value(), ListViewSampleHttpController::toDomain));
        };
    }

    /** JSON 배열에 {@code null} 요소가 섞여 들어올 수 있어 걸러낸다. 빈 목록은 아무것도 거르지 않는다. */
    private static List<com.example.springgrpc.server.service.dto.ListViewFilter> toTargets(
        List<ListViewFilter> targets) {
        if (targets == null) {
            return List.of();
        }
        return targets.stream()
            .map(ListViewSampleHttpController::toFilter)
            .filter(Objects::nonNull)
            .toList();
    }

    private static <W, D> Set<D> toDomainSet(List<W> values, Function<W, D> mapper) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 페이징. {@code null}은 "지정 안 함"이며, 도메인 레코드가 원시 타입이라 0으로 내린다.
     * 0 이하를 기본값으로 해석하는 것은 서비스의 몫이다.
     */
    private static ListViewQuery.Pagination toPagination(Pagination pagination) {
        if (pagination == null) {
            return null;
        }
        return new ListViewQuery.Pagination(
            pagination.pageSize() == null ? 0 : pagination.pageSize(),
            pagination.pageNumber() == null ? 0 : pagination.pageNumber());
    }

    /**
     * 정렬 키/순서는 본문에서 자유 문자열로 들어오므로 도메인 enum으로 좁힌다.
     * gRPC 어댑터와 같은 {@link EnumParser}를 쓴다 — 잘못된 값에 두 경로가 같은 메시지를 내야 한다.
     */
    private static ListViewQuery.Sort toSort(Sort sort) {
        if (sort == null) {
            return null;
        }
        return new ListViewQuery.Sort(
            EnumParser.parse(sort.sortKey(), "sort_key",
                ListViewQuery.Sort.Key.class, ListViewQuery.Sort.Key.ID),
            EnumParser.parse(sort.sortOrder(), "sort_order",
                ListViewQuery.Sort.Order.class, ListViewQuery.Sort.Order.ASC));
    }

    // ------------------------------------------------------------------
    // 요청용 enum(repository.enums) → 도메인 enum
    // ------------------------------------------------------------------

    private static ListViewEntry.Status toDomain(Status status) {
        return switch (status) {
            case PENDING -> ListViewEntry.Status.PENDING;
            case CREATING -> ListViewEntry.Status.CREATING;
            case CREATED -> ListViewEntry.Status.CREATED;
            case FAILED -> ListViewEntry.Status.FAILED;
            case DELETED -> ListViewEntry.Status.DELETED;
        };
    }

    private static ListViewEntry.Category toDomain(Category category) {
        return switch (category) {
            case BASIC -> ListViewEntry.Category.BASIC;
            case UNIFIED -> ListViewEntry.Category.UNIFIED;
            case QUERY -> ListViewEntry.Category.QUERY;
        };
    }

    private static ListViewEntry.Frequency toDomain(Frequency frequency) {
        return switch (frequency) {
            case IMMEDIATE -> ListViewEntry.Frequency.IMMEDIATE;
            case SPECIFIC_TIME -> ListViewEntry.Frequency.SPECIFIC_TIME;
            case EVERY_DAY -> ListViewEntry.Frequency.EVERY_DAY;
            case EVERY_WEEK -> ListViewEntry.Frequency.EVERY_WEEK;
            case EVERY_MONTH_DAY -> ListViewEntry.Frequency.EVERY_MONTH_DAY;
            case EVERY_YEAR -> ListViewEntry.Frequency.EVERY_YEAR;
        };
    }
}
