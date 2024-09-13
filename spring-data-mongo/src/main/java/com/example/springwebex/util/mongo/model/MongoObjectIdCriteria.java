package com.example.springwebex.util.mongo.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class MongoObjectIdCriteria {
    private String timezoneId;  // 'UTC', 'Asia/Seoul'
    private String startDate;   // '2011-12-03T10:15:30' DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private String endDate;     // '2011-12-03T10:15:30' DateTimeFormatter.ISO_LOCAL_DATE_TIME
}
