package com.example.springwebex.model.mongo.finder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class MongoCommonFindReq implements Serializable {

    private static final long serialVersionUID = -717033159421164288L;

// formatter: off
/*
    {
        "resp_timezone_id": "Asia/Seoul",
        "id_criteria": {
            "timezone_id": "Asia/Seoul",
            "start_date": "2024-01-01T00:00:00",
            "end_date": "2025-91-01T00:00:00"
        },
        "date_criterias": [
            {
                "timezone_id": "Asia/Seoul",
                "start_date": "2024-01-01T100:00:00.000",
                "end_date": "2025-01-01T100:00:00.000",
                "key": "server_time"
            }
        ],
        "value_criterias": {
            "intField": 0,
            "stringField": "hello"
        },
        "project_keys": [
            "_id",
            "intField",
            "server_time"
        ],
        "orders": {
            "_id": "desc",
            "intField": "asc"
        },
        "page_size": 1,
        "page_number": 100
    }
 */
// formatter: on

    private String collection;
    private String respTimezoneId; // response timezone id ex: Asia/Seoul
    private MongoObjectIdCriteria idCriteria;
    private List<MongoDateCriteria> dateCriterias;
    private Map<String, Object> equalCriterias;
    private Map<String, List<Object>> inCriterias;
    private Map<String, String> regexCriterias;
    private List<String> projectKeys;
    private Map<String, String> orders;
    private Integer pageSize;
    private Integer pageNumber;
}
