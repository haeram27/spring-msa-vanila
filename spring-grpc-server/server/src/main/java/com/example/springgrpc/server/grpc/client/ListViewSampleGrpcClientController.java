package com.example.springgrpc.server.grpc.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springgrpc.api.AndFilter;
import com.example.springgrpc.api.Category;
import com.example.springgrpc.api.CategoryFilter;
import com.example.springgrpc.api.FilterNode;
import com.example.springgrpc.api.Frequency;
import com.example.springgrpc.api.ListViewItem;
import com.example.springgrpc.api.ListViewRequest;
import com.example.springgrpc.api.ListViewResponse;
import com.example.springgrpc.api.Pagination;
import com.example.springgrpc.api.ScheduleFrequencyFilter;
import com.example.springgrpc.api.SearchStringFilter;
import com.example.springgrpc.api.Sort;
import com.example.springgrpc.api.Status;
import com.example.springgrpc.api.StatusFilter;
import com.example.springgrpc.client.ListViewSampleGrpcClient;
import com.example.springgrpc.server.util.HttpRequestLogger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST 요청을 {@code ListViewSample} gRPC 호출로 중계하는 어댑터.
 * {@code ListViewSampleHttpController}가 서비스를 직접 호출하는 방식과 비교하기 위한 경로로,
 * 같은 모양의 본문을 받아 같은 모양의 JSON을 돌려준다.
 *
 * <pre>
 * curl -X POST 'localhost:8080/api/grpc/client/list-view-sample' -H 'Content-Type: application/json' \
 *   -H 'x-tenant-id: T0001' -d '{"search":"traffic","status":["CREATED"]}'
 * </pre>
 */
@RestController
@RequestMapping("/api/grpc/client/list-view-sample")
public class ListViewSampleGrpcClientController {

    private final ListViewSampleGrpcClient listViewSampleGrpcClient;

    public ListViewSampleGrpcClientController(ListViewSampleGrpcClient listViewSampleGrpcClient) {
        this.listViewSampleGrpcClient = listViewSampleGrpcClient;
    }

    /**
     * 요청 본문. 도메인 타입이 아니라 proto enum으로 직접 바인딩한다. 어차피 proto 요청을 조립하는
     * 것이 이 클래스의 역할이라, 도메인을 한 번 거치면 enum 매핑만 중복된다.
     * <p>
     * 정렬 키/순서는 proto {@code Sort}가 자유 문자열로 정의하므로 문자열로 받아 그대로 넘기고,
     * 유효성은 서버 어댑터가 판정해 {@code INVALID_ARGUMENT}로 돌려준다.
     */
    public record Payload(
        String search,
        List<Status> status,
        List<Category> category,
        List<Frequency> frequency,
        Integer pageSize,
        Integer pageNumber,
        String sortKey,
        String sortOrder
    ) {
        public Payload {
            status = status == null ? List.of() : List.copyOf(status);
            category = category == null ? List.of() : List.copyOf(category);
            frequency = frequency == null ? List.of() : List.copyOf(frequency);
            pageSize = pageSize == null ? 0 : pageSize;
            pageNumber = pageNumber == null ? 0 : pageNumber;
            sortKey = sortKey == null || sortKey.isBlank() ? "ID" : sortKey;
            sortOrder = sortOrder == null || sortOrder.isBlank() ? "ASC" : sortOrder;
        }

        static Payload empty() {
            return new Payload(null, null, null, null, null, null, null, null);
        }
    }

    @PostMapping
    public Map<String, Object> listAll(@RequestBody(required = false) Payload body,
                                       @RequestHeader Map<String, String> headers,
                                       HttpServletRequest request) {
        HttpRequestLogger.logRequestMembers("ListViewSampleGrpcClientController#listAll", request);

        Payload payload = body == null ? Payload.empty() : body;

        ListViewRequest.Builder builder = ListViewRequest.newBuilder()
            .setPagination(Pagination.newBuilder()
                .setPageSize(payload.pageSize())
                .setPageNumber(payload.pageNumber()))
            .setSort(Sort.newBuilder()
                .setSortKey(payload.sortKey())
                .setSortOrder(payload.sortOrder()));

        FilterNode filter = toFilter(payload);
        if (filter != null) {
            builder.setFilter(filter);
        }

        ListViewResponse response = listViewSampleGrpcClient.listAll(
            builder.build(), ForwardableHeaders.from(headers));

        return toJson(response);
    }

    private static FilterNode toFilter(Payload payload) {
        List<FilterNode> targets = new ArrayList<>();
        if (payload.search() != null && !payload.search().isBlank()) {
            targets.add(FilterNode.newBuilder()
                .setSearchString(SearchStringFilter.newBuilder().setValue(payload.search()))
                .build());
        }
        if (!payload.status().isEmpty()) {
            targets.add(FilterNode.newBuilder()
                .setStatus(StatusFilter.newBuilder().addAllValues(payload.status()))
                .build());
        }
        if (!payload.category().isEmpty()) {
            targets.add(FilterNode.newBuilder()
                .setCategory(CategoryFilter.newBuilder().addAllValues(payload.category()))
                .build());
        }
        if (!payload.frequency().isEmpty()) {
            targets.add(FilterNode.newBuilder()
                .setFrequency(ScheduleFrequencyFilter.newBuilder().addAllValues(payload.frequency()))
                .build());
        }
        if (targets.isEmpty()) {
            return null;
        }
        return FilterNode.newBuilder()
            .setAnd(AndFilter.newBuilder().addAllTargets(targets))
            .build();
    }

    /**
     * proto 응답을 직접 호출 경로와 같은 모양의 JSON으로 옮긴다.
     * 도메인 enum과 proto enum의 상수 이름이 같으므로 {@code name()}만으로 두 경로의 값이 일치한다.
     */
    private static Map<String, Object> toJson(ListViewResponse response) {
        List<Map<String, String>> items = new ArrayList<>();
        for (ListViewItem item : response.getItemsList()) {
            Map<String, String> mapped = new LinkedHashMap<>();
            mapped.put("id", item.getId());
            mapped.put("name", item.getName());
            mapped.put("status", item.getStatus().name());
            mapped.put("category", item.getCategory().name());
            mapped.put("frequency", item.getFrequency().name());
            items.add(mapped);
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("items", items);
        json.put("totalCount", response.getTotalCount());
        return json;
    }
}
