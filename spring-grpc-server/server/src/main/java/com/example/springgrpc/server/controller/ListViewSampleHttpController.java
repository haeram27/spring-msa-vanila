package com.example.springgrpc.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springgrpc.server.controller.dto.ListViewRequestBody;
import com.example.springgrpc.server.service.ListViewSampleService;
import com.example.springgrpc.server.service.dto.ListViewFilter;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;
import com.example.springgrpc.server.util.HttpRequestLogger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST 어댑터: 같은 프로세스의 {@link ListViewSampleService}를 직접 호출한다.
 * {@code ListViewSampleGrpcServerService}와 동일한 서비스를 공유한다.
 *
 * <pre>
 * curl -X POST 'localhost:8080/api/list-view-sample' -H 'Content-Type: application/json' \
 *   -d '{"search":"traffic","status":["CREATED"],"sortKey":"NAME","sortOrder":"DESC"}'
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
    public ListViewResult listAll(@RequestBody(required = false) ListViewRequestBody body,
                                  HttpServletRequest request) {
        HttpRequestLogger.logRequestMembers("ListViewSampleHttpController#listAll", request);

        ListViewRequestBody effective = body == null ? ListViewRequestBody.empty() : body;

        ListViewQuery query = new ListViewQuery(
            toFilter(effective),
            new ListViewQuery.Pagination(effective.pageSize(), effective.pageNumber()),
            new ListViewQuery.Sort(effective.sortKey(), effective.sortOrder())
        );
        return listViewSampleService.listAll(query);
    }

    /** 지정된 조건만 골라 AND로 묶는다. 아무 조건도 없으면 {@code null}(=전체 조회)이다. */
    private static ListViewFilter toFilter(ListViewRequestBody body) {
        List<ListViewFilter> targets = new ArrayList<>();
        if (body.search() != null && !body.search().isBlank()) {
            targets.add(new ListViewFilter.SearchString(body.search()));
        }
        addIfPresent(targets, body.status(), values -> new ListViewFilter.StatusIn(Set.copyOf(values)));
        addIfPresent(targets, body.category(), values -> new ListViewFilter.CategoryIn(Set.copyOf(values)));
        addIfPresent(targets, body.frequency(), values -> new ListViewFilter.FrequencyIn(Set.copyOf(values)));
        return targets.isEmpty() ? null : new ListViewFilter.And(targets);
    }

    private static <E extends Enum<E>> void addIfPresent(List<ListViewFilter> targets,
                                                         List<E> values,
                                                         Function<List<E>, ListViewFilter> factory) {
        if (values != null && !values.isEmpty()) {
            targets.add(factory.apply(values));
        }
    }
}
