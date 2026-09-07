package com.example.springgrpc.server.controller.dto;

import java.util.List;

import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.FilterType;
import com.example.springgrpc.server.repository.enums.Status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ListViewFilter.AndFilter.class, name = "AND"),
        @JsonSubTypes.Type(value = ListViewFilter.SearchStringFilter.class, name = "SEARCH_STRING"),
        @JsonSubTypes.Type(value = ListViewFilter.StatusFilter.class, name = "STATUS"),
        @JsonSubTypes.Type(value = ListViewFilter.CategoryFilter.class, name = "CATEGORY"),
        @JsonSubTypes.Type(value = ListViewFilter.FrequencyFilter.class, name = "FREQUENCY"),})
public sealed interface ListViewFilter permits 
        ListViewFilter.AndFilter,
        ListViewFilter.SearchStringFilter,
        ListViewFilter.StatusFilter,
        ListViewFilter.CategoryFilter,
        ListViewFilter.FrequencyFilter {
    FilterType type();

    // ── 리프/재귀 노드 (record = 불변 값 객체) ──────────────────
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AndFilter(
            // : targets 리스트 각 요소(ListViewFilter)까지 재귀적으로 Bean Validation 수행
            // : null/빈 리스트를 허용하지 않음(최소 1개 요소 필요)
            List<ListViewFilter> targets, FilterType type) implements ListViewFilter {
        public AndFilter {
            type = FilterType.AND;
        } // 판별자 고정
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SearchStringFilter(
            // @NotBlank: null/빈 문자열/공백-only 문자열을 허용하지 않음
            String value, FilterType type) implements ListViewFilter {
        public SearchStringFilter {
            type = FilterType.SEARCH_STRING;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record StatusFilter(
            // : 최소 1개 상태값이 있어야 함
            List<Status> value, FilterType type) implements ListViewFilter {
        public StatusFilter {
            type = FilterType.STATUS;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CategoryFilter(
            // : 최소 1개 카테고리 값이 있어야 함
            List<Category> value, FilterType type) implements ListViewFilter {
        public CategoryFilter {
            type = FilterType.CATEGORY;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record FrequencyFilter(
            // : 최소 1개 주기 값이 있어야 함
            List<Frequency> value, FilterType type) implements ListViewFilter {
        public FrequencyFilter {
            type = FilterType.FREQUENCY;
        }
    }
}
