package com.example.springwebex.model.mongo.finder;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class MongoCommonFindReq implements Serializable {

    private static final long serialVersionUID = -717033159421164288L;

    private String respTimezoneId; // response timezone id ex: Asia/Seoul
    private MongoObjectIdCriteria idCriteria;
    private List<MongoDateCriteria> dateCriterias;
    private List<String> projectKeys;
    private Integer pageSize;
    private Integer pageNumber;
}
