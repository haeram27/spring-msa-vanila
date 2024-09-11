package com.example.springwebex.model.restreq;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class MongoCommonFindReqDto implements Serializable {

    private static final long serialVersionUID = -717033159421164288L;

    /*
        {
            "collection_name": "my_mongo_collection"
            "time_zone": "Asia/Seoul",
            "start_date": "2021-01-10T00:00:00",
            "end_date": "2024-12-31T00:00:00",
            "project_keys": [],
            "page_size": 5,
            "page_number": 0
        }
    */
    private String collectionName;
    private String timeZone;
    private String startDate;
    private String endDate;
    private List<String> projectKeys;
    private Integer pageSize;
    private Integer pageNumber;
}
