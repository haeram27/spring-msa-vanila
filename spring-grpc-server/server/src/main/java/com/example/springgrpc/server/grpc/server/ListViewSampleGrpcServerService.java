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
import com.example.springgrpc.api.ListViewSampleRequest;
import com.example.springgrpc.api.ListViewResponse;
import com.example.springgrpc.api.ListViewSampleGrpc;
import com.example.springgrpc.api.Pagination;
import com.example.springgrpc.api.Sort;
import com.example.springgrpc.api.Status;
import com.example.springgrpc.server.service.ListViewSampleService;
import com.example.springgrpc.server.service.dto.ListViewSampleEntry;
import com.example.springgrpc.server.service.dto.ListViewSampleFilter;
import com.example.springgrpc.server.service.dto.ListViewSampleQuery;
import com.example.springgrpc.server.service.dto.ListViewSampleResult;
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
    public void listAll(ListViewSampleRequest request, StreamObserver<ListViewResponse> responseObserver) {
        GrpcRequestLogger.logRequestMembers("ListViewSampleGrpcServerService#listAll", request);
        GrpcCallExecutor.execute("ListViewSampleGrpcServerService#listAll", responseObserver, () -> {
            ListViewSampleResult result = listViewSampleService.list(toQuery(request));
            return toResponse(result);
        });
    }

    // ------------------------------------------------------------------
    // proto → domain
    // ------------------------------------------------------------------

    private static ListViewSampleQuery toQuery(ListViewSampleRequest request) {
        return new ListViewSampleQuery(
            request.hasFilter() ? toFilter(request.getFilter()) : null,
            request.hasPagination() ? toPagination(request.getPagination()) : null,
            request.hasSort() ? toSort(request.getSort()) : null
        );
    }

    /** oneof가 설정되지 않은 노드는 조건 없음으로 보고 {@code null}을 돌려준다. */
    private static ListViewSampleFilter toFilter(FilterNode node) {
        return switch (node.getConditionCase()) {
            case AND -> new ListViewSampleFilter.And(
                node.getAnd().getTargetsList().stream()
                    .map(ListViewSampleGrpcServerService::toFilter)
                    .filter(Objects::nonNull)
                    .toList());
            case SEARCH_STRING -> new ListViewSampleFilter.SearchString(node.getSearchString().getValue());
            case STATUS -> new ListViewSampleFilter.StatusIn(
                mapEnums(node.getStatus().getValuesList(), ListViewSampleGrpcServerService::toDomain));
            case CATEGORY -> new ListViewSampleFilter.CategoryIn(
                mapEnums(node.getCategory().getValuesList(), ListViewSampleGrpcServerService::toDomain));
            case FREQUENCY -> new ListViewSampleFilter.FrequencyIn(
                mapEnums(node.getFrequency().getValuesList(), ListViewSampleGrpcServerService::toDomain));
            case CONDITION_NOT_SET -> null;
        };
    }

    private static ListViewSampleQuery.Pagination toPagination(Pagination pagination) {
        return new ListViewSampleQuery.Pagination(pagination.getPageSize(), pagination.getPageNumber());
    }

    /**
     * proto의 정렬 키/순서는 자유 문자열이라 여기서 도메인 enum으로 좁힌다.
     * 값이 비면 기본값(id 오름차순), 알 수 없는 값이면 {@code INVALID_ARGUMENT}로 매핑될 예외를 던진다.
     */
    private static ListViewSampleQuery.Sort toSort(Sort sort) {
        return new ListViewSampleQuery.Sort(
            EnumParser.parse(sort.getSortKey(), "sort_key", ListViewSampleQuery.Sort.Key.class, ListViewSampleQuery.Sort.Key.ID),
            EnumParser.parse(sort.getSortOrder(), "sort_order", ListViewSampleQuery.Sort.Order.class, ListViewSampleQuery.Sort.Order.ASC)
        );
    }

    private static <P, D> Set<D> mapEnums(List<P> values, java.util.function.Function<P, D> mapper) {
        return values.stream()
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static com.example.springgrpc.server.repository.enums.Status toDomain(Status status) {
        return switch (status) {
            case PENDING -> com.example.springgrpc.server.repository.enums.Status.PENDING;
            case CREATING -> com.example.springgrpc.server.repository.enums.Status.CREATING;
            case CREATED -> com.example.springgrpc.server.repository.enums.Status.CREATED;
            case FAILED -> com.example.springgrpc.server.repository.enums.Status.FAILED;
            case DELETED -> com.example.springgrpc.server.repository.enums.Status.DELETED;
            case STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private static com.example.springgrpc.server.repository.enums.Category toDomain(Category category) {
        return switch (category) {
            case BASIC -> com.example.springgrpc.server.repository.enums.Category.BASIC;
            case UNIFIED -> com.example.springgrpc.server.repository.enums.Category.UNIFIED;
            case QUERY -> com.example.springgrpc.server.repository.enums.Category.QUERY;
            case CATEGORY_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private static com.example.springgrpc.server.repository.enums.Frequency toDomain(Frequency frequency) {
        return switch (frequency) {
            case IMMEDIATE -> com.example.springgrpc.server.repository.enums.Frequency.IMMEDIATE;
            case SPECIFIC_TIME -> com.example.springgrpc.server.repository.enums.Frequency.SPECIFIC_TIME;
            case EVERY_DAY -> com.example.springgrpc.server.repository.enums.Frequency.EVERY_DAY;
            case EVERY_WEEK -> com.example.springgrpc.server.repository.enums.Frequency.EVERY_WEEK;
            case EVERY_MONTH_DAY -> com.example.springgrpc.server.repository.enums.Frequency.EVERY_MONTH_DAY;
            case EVERY_YEAR -> com.example.springgrpc.server.repository.enums.Frequency.EVERY_YEAR;
            case FREQUENCY_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    // ------------------------------------------------------------------
    // domain → proto
    // ------------------------------------------------------------------

    private static ListViewResponse toResponse(ListViewSampleResult result) {
        return ListViewResponse.newBuilder()
            .addAllItems(result.items().stream().map(ListViewSampleGrpcServerService::toProto).toList())
            .setTotalCount(result.totalCount())
            .build();
    }

    private static ListViewItem toProto(ListViewSampleEntry entry) {
        return ListViewItem.newBuilder()
            .setId(entry.id())
            .setName(entry.name())
            .setStatus(toProto(entry.status()))
            .setCategory(toProto(entry.category()))
            .setFrequency(toProto(entry.frequency()))
            .build();
    }

    private static Status toProto(com.example.springgrpc.server.repository.enums.Status status) {
        return switch (status) {
            case PENDING -> Status.PENDING;
            case CREATING -> Status.CREATING;
            case CREATED -> Status.CREATED;
            case FAILED -> Status.FAILED;
            case DELETED -> Status.DELETED;
        };
    }

    private static Category toProto(com.example.springgrpc.server.repository.enums.Category category) {
        return switch (category) {
            case BASIC -> Category.BASIC;
            case UNIFIED -> Category.UNIFIED;
            case QUERY -> Category.QUERY;
        };
    }

    private static Frequency toProto(com.example.springgrpc.server.repository.enums.Frequency frequency) {
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
