package com.example.springgrpc.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springgrpc.server.repository.ListViewItemSpecificationRepository;
import com.example.springgrpc.server.service.dto.ListViewQuery;
import com.example.springgrpc.server.service.dto.ListViewResult;

/**
 * 목록 조회 서비스. 생략된 조회 조건의 기본값을 정하고 영속성 어댑터에 위임한다.
 * <p>
 * gRPC 어댑터({@code ListViewSampleGrpcServerService})는 proto 매핑과 검증만,
 * {@link ListViewItemSpecificationRepository}는 Specification 조립만 담당한다. 페이지 크기
 * 기본값처럼 "업무 규칙"에 해당하는 판단은 여기 남는다. {@link HelloService}와 같은 구조다.
 * <p>
 * 페이징 기본값을 여기서 확정해 넘기는 이유 — 어댑터가 {@code pageNumber <= 0}을 스스로 해석하면
 * 같은 계약을 두 곳에서 정의하게 된다. 어댑터는 이미 정규화된 값만 받는다.
 */
@Service
public class ListViewSampleService {

    private static final Logger log = LoggerFactory.getLogger(ListViewSampleService.class);

    /** {@code page_size}가 지정되지 않았을 때 쓰는 값. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ListViewItemSpecificationRepository listViewItemSpecificationRepository;

    public ListViewSampleService(ListViewItemSpecificationRepository listViewItemSpecificationRepository) {
        this.listViewItemSpecificationRepository = listViewItemSpecificationRepository;
    }

    @Transactional(readOnly = true)
    public ListViewResult list(ListViewQuery query) {
        log.info("ListViewSampleService#list called with query={}", query);

        ListViewQuery effective = query == null ? ListViewQuery.all() : query;
        ListViewQuery.Pagination pagination = effective.pagination();

        int pageSize = pagination == null || pagination.pageSize() <= 0
            ? DEFAULT_PAGE_SIZE
            : pagination.pageSize();
        int pageNumber = pagination == null || pagination.pageNumber() <= 0
            ? 1
            : pagination.pageNumber();

        return listViewItemSpecificationRepository.search(effective, pageNumber, pageSize);
    }
}
