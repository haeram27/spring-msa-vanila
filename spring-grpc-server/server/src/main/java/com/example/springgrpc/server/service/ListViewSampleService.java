package com.example.springgrpc.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springgrpc.server.repository.ListViewSampleItemEntity;
import com.example.springgrpc.server.repository.ListViewSampleItemRepository;
import com.example.springgrpc.server.repository.ListViewSampleItemSpecificationRepository;
import com.example.springgrpc.server.service.dto.ListViewSampleEntry;
import com.example.springgrpc.server.service.dto.ListViewSampleQuery;
import com.example.springgrpc.server.service.dto.ListViewSampleResult;

/**
 * 목록 조회 서비스. 생략된 조회 조건의 기본값을 정하고 영속성 어댑터에 위임한다.
 * <p>
 * gRPC 어댑터({@code ListViewSampleGrpcServerService})는 proto 매핑과 검증만,
 * {@link ListViewSampleItemSpecificationRepository}는 Specification 조립만 담당한다. 페이지 크기
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

    private final ListViewSampleItemRepository listViewItemRepository;
    private final ListViewSampleItemSpecificationRepository listViewItemSpecificationRepository;

    public ListViewSampleService(ListViewSampleItemRepository listViewItemRepository,
                                 ListViewSampleItemSpecificationRepository listViewItemSpecificationRepository) {
        this.listViewItemRepository = listViewItemRepository;
        this.listViewItemSpecificationRepository = listViewItemSpecificationRepository;
    }

    @Transactional
    public ListViewSampleEntry create(ListViewSampleEntry entry) {
        validateEntry(entry, false);
        ListViewSampleItemEntity created = listViewItemRepository.save(
            new ListViewSampleItemEntity(entry.name(), entry.status(), entry.category(), entry.frequency()));
        return created.toDomain();
    }

    @Transactional(readOnly = true)
    public ListViewSampleEntry getById(Long id) {
        validateId(id);
        return listViewItemRepository.findById(id)
            .map(ListViewSampleItemEntity::toDomain)
            .orElseThrow(() -> new IllegalStateException("ListView item not found: id=" + id));
    }

    @Transactional
    public ListViewSampleEntry update(ListViewSampleEntry entry) {
        validateEntry(entry, true);
        ListViewSampleItemEntity entity = listViewItemRepository.findById(entry.id())
            .orElseThrow(() -> new IllegalStateException("ListView item not found: id=" + entry.id()));
        entity.update(entry.name(), entry.status(), entry.category(), entry.frequency());
        return entity.toDomain();
    }

    @Transactional
    public void deleteById(Long id) {
        validateId(id);
        if (!listViewItemRepository.existsById(id)) {
            throw new IllegalStateException("ListView item not found: id=" + id);
        }
        listViewItemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ListViewSampleResult list(ListViewSampleQuery query) {
        log.info("ListViewSampleService#list called with query={}", query);

        ListViewSampleQuery effective = query == null ? ListViewSampleQuery.all() : query;
        ListViewSampleQuery.Pagination pagination = effective.pagination();

        int pageSize = pagination == null || pagination.pageSize() <= 0
            ? DEFAULT_PAGE_SIZE
            : pagination.pageSize();
        int pageNumber = pagination == null || pagination.pageNumber() <= 0
            ? 1
            : pagination.pageNumber();

        return listViewItemSpecificationRepository.search(effective, pageNumber, pageSize);
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
    }

    private static void validateEntry(ListViewSampleEntry entry, boolean idRequired) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        if (idRequired) {
            validateId(entry.id());
        } else if (entry.id() != null) {
            throw new IllegalArgumentException("id must be null for create");
        }
        if (entry.name() == null || entry.name().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (entry.status() == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (entry.category() == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        if (entry.frequency() == null) {
            throw new IllegalArgumentException("frequency must not be null");
        }
    }
}
