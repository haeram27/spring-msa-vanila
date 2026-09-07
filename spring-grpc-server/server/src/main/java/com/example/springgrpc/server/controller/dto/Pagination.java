package com.example.springgrpc.server.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Pagination(
    Integer pageSize,
    // valid minimum pageNumber is 1
    Integer pageNumber
) {}