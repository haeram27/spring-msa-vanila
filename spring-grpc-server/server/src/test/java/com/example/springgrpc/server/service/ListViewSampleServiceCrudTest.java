package com.example.springgrpc.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.springgrpc.server.repository.ListViewSampleItemEntity;
import com.example.springgrpc.server.repository.ListViewSampleItemRepository;
import com.example.springgrpc.server.repository.ListViewSampleItemSpecificationRepository;
import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.Status;
import com.example.springgrpc.server.service.dto.ListViewSampleEntry;

@DataJpaTest
@Import({ ListViewSampleService.class, ListViewSampleItemSpecificationRepository.class })
class ListViewSampleServiceCrudTest {

    @Autowired
    private ListViewSampleService service;

    @Autowired
    private ListViewSampleItemRepository repository;

    private Long dailyId;
    private Long weeklyId;

    @BeforeEach
    void seed() {
        List<ListViewSampleItemEntity> seeded = repository.saveAll(List.of(
            new ListViewSampleItemEntity("Daily Traffic Report",
                Status.CREATED, Category.BASIC, Frequency.EVERY_DAY),
            new ListViewSampleItemEntity("Weekly Threat Summary",
                Status.PENDING, Category.UNIFIED, Frequency.EVERY_WEEK)
        ));
        dailyId = seeded.get(0).getId();
        weeklyId = seeded.get(1).getId();
    }

    @Test
    @DisplayName("create 는 DB 시퀀스로 id를 생성한다")
    void createGeneratesId() {
        ListViewSampleEntry created = service.create(new ListViewSampleEntry(
            null, "Ad-hoc Traffic Query", Status.CREATING, Category.QUERY, Frequency.IMMEDIATE));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Ad-hoc Traffic Query");
    }

    @Test
    @DisplayName("getById 는 저장된 항목을 돌려준다")
    void getByIdReturnsEntry() {
        ListViewSampleEntry found = service.getById(dailyId);

        assertThat(found.id()).isEqualTo(dailyId);
        assertThat(found.name()).isEqualTo("Daily Traffic Report");
    }

    @Test
    @DisplayName("update 는 기존 항목을 수정한다")
    void updateModifiesExistingEntry() {
        ListViewSampleEntry updated = service.update(new ListViewSampleEntry(
            weeklyId, "Weekly Threat Summary Updated", Status.CREATED, Category.UNIFIED, Frequency.EVERY_WEEK));

        assertThat(updated.name()).isEqualTo("Weekly Threat Summary Updated");
        assertThat(repository.findById(weeklyId).orElseThrow().getName()).isEqualTo("Weekly Threat Summary Updated");
    }

    @Test
    @DisplayName("deleteById 는 항목을 물리 삭제한다")
    void deleteRemovesEntry() {
        service.deleteById(dailyId);

        assertThat(repository.findById(dailyId)).isEmpty();
    }

    @Test
    @DisplayName("create 에 id를 주면 거부한다")
    void rejectClientProvidedIdOnCreate() {
        assertThatThrownBy(() -> service.create(new ListViewSampleEntry(
            999L, "invalid", Status.PENDING, Category.BASIC, Frequency.EVERY_DAY)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("id must be null for create");
    }

    @Test
    @DisplayName("미존재 항목 update/delete/get 은 명시적 예외를 던진다")
    void notFoundCrudOperationsFailExplicitly() {
        assertThatThrownBy(() -> service.getById(9999L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
        assertThatThrownBy(() -> service.update(new ListViewSampleEntry(
            9999L, "missing", Status.PENDING, Category.BASIC, Frequency.EVERY_DAY)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
        assertThatThrownBy(() -> service.deleteById(9999L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
    }
}
