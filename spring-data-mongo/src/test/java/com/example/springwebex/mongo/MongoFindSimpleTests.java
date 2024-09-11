package com.example.springwebex.mongo;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest  // this annotation support only embedded mongo
@Slf4j
public class MongoFindSimpleTests {
    private final String COLLECTION_NAME = "samples";

    @Autowired
    private MongoTemplate mongoTemplate; // mongoTemplate is overrided by @TestConfiguration

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

    @SuppressWarnings("unused")
    @Test
    public void findTest() {
        Query query = new Query();

        /* Filter */
        // DATE/TIME Range
        if (false) {
            Date fromDate = Date.from(Instant.parse("2024-08-01T04:50:09.511Z"));
            Date toDate = Date.from(Instant.parse("2024-08-01T04:50:09.513Z"));
            query.addCriteria(Criteria.where("dateField").gte(fromDate).lt(toDate));
        }
        // $eq matching
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("booleanField", true);
        for (String key : conditionMap.keySet()) {
            query.addCriteria(Criteria.where(key).is(conditionMap.get(key)));
        }

        // regex matching
        query.addCriteria(Criteria.where("stringField").regex("^Sam.*\\d{4}$"));

        /* Project */
        List<String> includeFieldList = Arrays.asList("_id", "dateField");
        for (String field : includeFieldList) {
            query.fields().include(field); // include() or exclude()
        }

        /* Sorting and Pagenation */
        int pageNumber = 0; // zero-based page number, must not be negative.
        int pageSize = 5; // number of docs per a page to be returned, must be greater than 0.
        Sort sort = Sort.by(Sort.Direction.DESC, "_id");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        query.with(pageable);

        /* Get Results */
        List<Document> results = mongoTemplate.find(query, Document.class, COLLECTION_NAME);
        System.out.println("Size of Results: " + results.size());
    }
}
