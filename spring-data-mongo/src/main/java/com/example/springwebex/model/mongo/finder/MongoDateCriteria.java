package com.example.springwebex.model.mongo.finder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@Data
public class MongoDateCriteria {
    private String timeZone;
    private String key;
    private String startDate; // '2011-12-03T10:15:30.123' DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private String endDate; // '2011-12-03T10:15:30.123' DateTimeFormatter.ISO_LOCAL_DATE_TIME
}
