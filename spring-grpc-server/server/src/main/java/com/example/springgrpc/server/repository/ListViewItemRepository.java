package com.example.springgrpc.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * {@code tb_list_view}의 Spring Data 리포지토리.
 * <p>
 * {@link JpaSpecificationExecutor}만 있으면 되지만 {@link JpaRepository}도 함께 상속해
 * 테스트에서 {@code saveAll}로 데이터를 넣을 수 있게 한다.
 * <p>
 * 이 인터페이스를 추가해도 스키마는 바뀌지 않는다. 테이블을 만드는 것은 {@code @Entity} 매핑을
 * 재료로 삼는 Hibernate의 {@code ddl-auto}다.
 */
public interface ListViewItemRepository
    extends JpaRepository<ListViewItemEntity, String>, JpaSpecificationExecutor<ListViewItemEntity> {
}
