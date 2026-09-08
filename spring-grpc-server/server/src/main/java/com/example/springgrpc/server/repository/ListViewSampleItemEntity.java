package com.example.springgrpc.server.repository;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.Status;
import com.example.springgrpc.server.service.dto.ListViewSampleEntry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 목록 항목의 영속 표현. 도메인 타입({@link ListViewSampleEntry})과 필드가 같지만 분리해 둔다.
 * <p>
 * 도메인 레코드는 불변이라 JPA가 요구하는 기본 생성자/가변 필드와 맞지 않고, 컬럼명이나 인덱스
 * 같은 저장소 사정이 도메인에 스며드는 것도 막는다. 변환은 {@link #toDomain()} 한곳에서 한다.
 * <p>
 * enum은 {@link EnumType#STRING}으로 저장한다. ORDINAL은 상수 순서가 바뀌는 순간 기존 데이터의
 * 의미가 조용히 달라진다.
 */
@Entity
@Table(name = "tb_list_view_sample")
public class ListViewSampleItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_list_view_sample")
    @SequenceGenerator(name = "seq_list_view_sample", sequenceName = "seq_list_view_sample_id", allocationSize = 1)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Frequency frequency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant modifiedAt;

    /** JPA 전용. 직접 쓰지 않는다. */
    protected ListViewSampleItemEntity() {
    }

    public ListViewSampleItemEntity(String name,
                              Status status,
                              Category category,
                              Frequency frequency) {
        this(null, name, status, category, frequency);
    }

    public ListViewSampleItemEntity(Long id,
                              String name,
                              Status status,
                              Category category,
                              Frequency frequency) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.category = category;
        this.frequency = frequency;
    }

    public ListViewSampleEntry toDomain() {
        return new ListViewSampleEntry(id, name, status, category, frequency);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void update(String name, Status status, Category category, Frequency frequency) {
        this.name = name;
        this.status = status;
        this.category = category;
        this.frequency = frequency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }
}
