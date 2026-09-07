package com.example.springgrpc.server.grpc.server;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.grpc.server.service.GrpcService;

import com.example.springgrpc.api.Category;
import com.example.springgrpc.api.FilterNode;
import com.example.springgrpc.api.Frequency;
import com.example.springgrpc.api.ListViewItem;
import com.example.springgrpc.api.ListViewRequest;
import com.example.springgrpc.api.ListViewResponse;
import com.example.springgrpc.api.ListViewSampleGrpc;
import com.example.springgrpc.api.Pagination;
import com.example.springgrpc.api.Sort;
import com.example.springgrpc.api.Status;
import com.example.springgrpc.server.service.ListViewSampleService;
import com.example.springgrpc.server.service.dto.ListViewEntry;
import com.example.springgrpc.server.service.dto.ListViewFilter;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;
import com.example.springgrpc.server.util.EnumParser;
import com.example.springgrpc.server.util.GrpcCallExecutor;
import com.example.springgrpc.server.util.GrpcRequestLogger;

import io.grpc.stub.StreamObserver;

/**
 * {@code com.example.grpc.v1.ListViewSample}의 서버 어댑터.
 * proto ↔ 도메인 매핑만 담당하고 조회 로직은 {@link ListViewSampleService}에 맡긴다.
 * <p>
 * enum 매핑을 {@code valueOf(name())} 대신 명시적 switch로 쓴 이유는, proto에 상수가 추가되면
 * 런타임에 조용히 실패하는 대신 이 파일이 컴파일 에러로 먼저 드러나게 하기 위해서다.
 */
@GrpcService
public class ListViewSampleGrpcServerService extends ListViewSampleGrpc.ListViewSampleImplBase {

    private final ListViewSampleService listViewSampleService;

    public ListViewSampleGrpcServerService(ListViewSampleService listViewSampleService) {
        this.listViewSampleService = listViewSampleService;
    }

/*
grpcurl -plaintext \
-H 'x-tenant-id: T0001' \
-d '{"filter":{"and":{"targets":[{"searchString":{"value":"traffic"}},{"status":{"values":["CREATED"]}}]}},"pagination":{"pageSize":10,"pageNumber":1},"sort":{"sortKey":"name","sortOrder":"asc"}}' \
localhost:9090 \
com.example.grpc.v1.ListViewSample/ListAll
*/
    @Override
    public void listAll(ListViewRequest request, StreamObserver<ListViewResponse> responseObserver) {
        GrpcRequestLogger.logRequestMembers("ListViewSampleGrpcServerService#listAll", request);
        GrpcCallExecutor.execute("ListViewSampleGrpcServerService#listAll", responseObserver, () -> {
            ListViewResult result = listViewSampleService.list(toQuery(request));
            return toResponse(result);
        });
    }

    // ------------------------------------------------------------------
    // proto → domain
    // ------------------------------------------------------------------

    private static ListViewQuery toQuery(ListViewRequest request) {
        return new ListViewQuery(
            request.hasFilter() ? toFilter(request.getFilter()) : null,
            request.hasPagination() ? toPagination(request.getPagination()) : null,
            request.hasSort() ? toSort(request.getSort()) : null
        );
    }

    /** oneof가 설정되지 않은 노드는 조건 없음으로 보고 {@code null}을 돌려준다. */
    private static ListViewFilter toFilter(FilterNode node) {
        return switch (node.getConditionCase()) {
            case AND -> new ListViewFilter.And(
                node.getAnd().getTargetsList().stream()
                    .map(ListViewSampleGrpcServerService::toFilter)
                    .filter(Objects::nonNull)
                    .toList());
            case SEARCH_STRING -> new ListViewFilter.SearchString(node.getSearchString().getValue());
            case STATUS -> new ListViewFilter.StatusIn(
                mapEnums(node.getStatus().getValuesList(), ListViewSampleGrpcServerService::toDomain));
            case CATEGORY -> new ListViewFilter.CategoryIn(
                mapEnums(node.getCategory().getValuesList(), ListViewSampleGrpcServerService::toDomain));
            case FREQUENCY -> new ListViewFilter.FrequencyIn(
                mapEnums(node.getFrequency().getValuesList(), ListViewSampleGrpcServerService::toDomain));
            case CONDITION_NOT_SET -> null;
        };
    }

    private static ListViewQuery.Pagination toPagination(Pagination pagination) {
        return new ListViewQuery.Pagination(pagination.getPageSize(), pagination.getPageNumber());
    }

    /**
     * proto의 정렬 키/순서는 자유 문자열이라 여기서 도메인 enum으로 좁힌다.
     * 값이 비면 기본값(id 오름차순), 알 수 없는 값이면 {@code INVALID_ARGUMENT}로 매핑될 예외를 던진다.
     */
    private static ListViewQuery.Sort toSort(Sort sort) {
        return new ListViewQuery.Sort(
            EnumParser.parse(sort.getSortKey(), "sort_key", ListViewQuery.Sort.Key.class, ListViewQuery.Sort.Key.ID),
            EnumParser.parse(sort.getSortOrder(), "sort_order", ListViewQuery.Sort.Order.class, ListViewQuery.Sort.Order.ASC)
        );
    }

    private static <P, D> Set<D> mapEnums(List<P> values, java.util.function.Function<P, D> mapper) {
        return values.stream()
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static ListViewEntry.Status toDomain(Status status) {
        return switch (status) {
            case PENDING -> ListViewEntry.Status.PENDING;
            case CREATING -> ListViewEntry.Status.CREATING;
            case CREATED -> ListViewEntry.Status.CREATED;
            case FAILED -> ListViewEntry.Status.FAILED;
            case DELETED -> ListViewEntry.Status.DELETED;
            case STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private static ListViewEntry.Category toDomain(Category category) {
        return switch (category) {
            case BASIC -> ListViewEntry.Category.BASIC;
            case UNIFIED -> ListViewEntry.Category.UNIFIED;
            case QUERY -> ListViewEntry.Category.QUERY;
            case CATEGORY_UNSPECIFIED, UNRECOGNIZED -> null;
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
            case FREQUENCY_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    // ------------------------------------------------------------------
    // domain → proto
    // ------------------------------------------------------------------

    private static ListViewResponse toResponse(ListViewResult result) {
        return ListViewResponse.newBuilder()
            .addAllItems(result.items().stream().map(ListViewSampleGrpcServerService::toProto).toList())
            .setTotalCount(result.totalCount())
            .build();
    }

    private static ListViewItem toProto(ListViewEntry entry) {
        return ListViewItem.newBuilder()
            .setId(entry.id())
            .setName(entry.name())
            .setStatus(toProto(entry.status()))
            .setCategory(toProto(entry.category()))
            .setFrequency(toProto(entry.frequency()))
            .build();
    }

    private static Status toProto(ListViewEntry.Status status) {
        return switch (status) {
            case PENDING -> Status.PENDING;
            case CREATING -> Status.CREATING;
            case CREATED -> Status.CREATED;
            case FAILED -> Status.FAILED;
            case DELETED -> Status.DELETED;
        };
    }

    private static Category toProto(ListViewEntry.Category category) {
        return switch (category) {
            case BASIC -> Category.BASIC;
            case UNIFIED -> Category.UNIFIED;
            case QUERY -> Category.QUERY;
        };
    }

    private static Frequency toProto(ListViewEntry.Frequency frequency) {
        return switch (frequency) {
            case IMMEDIATE -> Frequency.IMMEDIATE;
            case SPECIFIC_TIME -> Frequency.SPECIFIC_TIME;
            case EVERY_DAY -> Frequency.EVERY_DAY;
            case EVERY_WEEK -> Frequency.EVERY_WEEK;
            case EVERY_MONTH_DAY -> Frequency.EVERY_MONTH_DAY;
            case EVERY_YEAR -> Frequency.EVERY_YEAR;
        };
    }
}
