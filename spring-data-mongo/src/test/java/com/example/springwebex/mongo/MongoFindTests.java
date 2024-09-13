package com.example.springwebex.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.example.springwebex.model.mongo.MongoSampleDoc;
import com.example.springwebex.util.mongo.MongoFindUtils;
import com.example.springwebex.util.mongo.MongoFindUtils.DateTimeConverter;
import com.example.springwebex.util.mongo.MongoFindUtils.ObjectIdConverter;
import com.example.springwebex.util.mongo.model.MongoCommonFindReq;
import com.example.springwebex.util.mongo.model.MongoDateCriteria;
import com.example.springwebex.util.mongo.model.MongoObjectIdCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class MongoFindTests {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        // https://www.baeldung.com/spring-beans-integration-test-override
        // https://howtodoinjava.com/spring-boot2/testing/springboot-test-configuration/

        @Primary
        @Bean("mongoTemplate.stub")
        public MongoTemplate mongoTemplate() throws Exception {
            ConnectionString connectionString = new ConnectionString(
                    "mongodb://localhost:27017/test");

            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            return new MongoTemplate(MongoClients.create(mongoClientSettings), "test");
        }
    }

    @Test
    public void findTest() {
        // define criteria
        var req = new MongoCommonFindReq();
        req.setCollection("samples");
/*
        req.setRespTimezoneId("Asia/Seoul");

        var id = new MongoObjectIdCriteria();
        id.setTimezoneId("Asia/Seoul");
        id.setStartDate("2024-01-01T00:00:00");
        id.setEndDate("2024-12-31T00:00:00");
        req.setIdCriteria(id);

        var dateCriterias = new ArrayList<MongoDateCriteria>();
        var date = new MongoDateCriteria();
        date.setKey("createdAt");
        date.setTimezoneId("Asia/Seoul");
        date.setStartDate("2024-01-01T00:00:00.000");
        date.setEndDate("2024-12-31T00:00:00.000");
        dateCriterias.add(date);
        req.setDateCriterias(dateCriterias);

        var projectKeys = new ArrayList<String>();
        projectKeys.add("_id");
        projectKeys.add("createdAt");
        projectKeys.add("intField");
        req.setProjectKeys(projectKeys);

        var equalCriterias = new LinkedHashMap<String, Object>();
        equalCriterias.put("intField", 0);
        req.setEqualCriterias(equalCriterias);
*/
        var inCriterias = new LinkedHashMap<String, List<Object>>();
        inCriterias.put("intField", Arrays.asList(0, 1));
        req.setInCriterias(inCriterias);

        var orders = new LinkedHashMap<String, String>();
        orders.put("intField", "asc");
        req.setOrders(orders);

        req.setPageSize(100);
        req.setPageNumber(0);

        // query
        var results = MongoFindUtils.find(mongoTemplate, req);
        var mappedResults = MongoFindUtils.mapDocuments(results, null,
                new MongoFindUtils.ObjectIdConverter(),
                new MongoFindUtils.DateTimeConverter(req.getRespTimezoneId()));
 
        mappedResults.stream().forEach((map) -> {
            try {
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
            } catch (JsonProcessingException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }

    @Test
    public void findClazzTest() {
        var req = new MongoCommonFindReq();
        req.setCollection("samples");
        req.setRespTimezoneId("Asia/Seoul");

        var id = new MongoObjectIdCriteria();
        id.setTimezoneId("Asia/Seoul");
        id.setStartDate("2024-01-01T00:00:00");
        id.setEndDate("2024-12-31T00:00:00");
        req.setIdCriteria(id);

        var dateCriterias = new ArrayList<MongoDateCriteria>();
        var date = new MongoDateCriteria();
        date.setKey("createdAt");
        date.setTimezoneId("Asia/Seoul");
        date.setStartDate("2024-01-01T00:00:00.000");
        date.setEndDate("2024-12-31T00:00:00.000");
        dateCriterias.add(date);
        req.setDateCriterias(dateCriterias);

        var projectKeys = new ArrayList<String>();
        projectKeys.add("_id");
        projectKeys.add("createdAt");
        projectKeys.add("intField");
        req.setProjectKeys(projectKeys);

        var orders = new LinkedHashMap<String, String>();
        orders.put("intField", "asc");
        req.setOrders(orders);

        req.setPageSize(100);
        req.setPageNumber(0);

        // query
        var results = MongoFindUtils.find(mongoTemplate, req, MongoSampleDoc.class);
        results.stream().forEach((obj) -> {
            System.out.println(obj);
        });
    }
}
