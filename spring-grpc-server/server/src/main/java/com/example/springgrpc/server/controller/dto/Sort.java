package com.example.springgrpc.server.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Sort(
    String sortKey,
    String sortOrder     // 또는 enum SortOrder { ASC, DESC }
) {}