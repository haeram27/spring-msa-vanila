package com.example.springwebex.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
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

import com.jayway.jsonpath.internal.JsonFormatter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest  // this annotation support only embedded mongo
@Slf4j
public class MongoFindTests {
    private final String COLLECTION_NAME = "samples";

    @Autowired
    private MongoTemplate mongoTemplate;

    // https://www.baeldung.com/spring-beans-integration-test-override
    // https://howtodoinjava.com/spring-boot2/testing/springboot-test-configuration/
    @TestConfiguration
    static class TestConfig {
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

    /*  
     *  # find query configuration order
     *  criteria (where)
     *  project (select)
     *  sorting (default: ASC by $oid, recomm: DESC)
     *  pagenation (default: none)
     */

    @SuppressWarnings("unused")
    @Test
    public void findTest() {
        Query query = new Query();

        // # match (criteria, document filtering)
        // criteria: value is
        if (true) {
            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put("booleanField", true);
            for (String key : conditionMap.keySet()) {
                query.addCriteria(Criteria.where(key).is(conditionMap.get(key)));
            }
        }

        // criteria: _id made in latest 24 hours
        if (false) {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            Instant instant = twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant();
            ObjectId objectId = new ObjectId(Date.from(instant));
            query.addCriteria(Criteria.where("_id").gt(objectId));
        }

        /* # project (field filtering)
         * include vs exclude ?
         * include and exclude field can not be configured simutaneously
         * only last include or exclude definition is stored in query
         */
        String projectType = "NONE";
        if ("INCLUDE".equals(projectType)) {
            // # project: include
            List<String> includeFieldList = Arrays.asList("_id", "objectIdField", "booleanField");
            for (String field : includeFieldList) {
                query.fields().include(field); // include also ok
            }
        } else if ("EXCLUDE".equals(projectType)) {
            // # project: exclude
            List<String> excludeFieldList = Arrays.asList("objectIdField", "booleanField");
            for (String field : excludeFieldList) {
                query.fields().exclude(field); // include also ok
            }
        }

        /* 
            !!! WARNING:
            Query.with(Pageable) sets sort, skip, limit in Query internally
            so do NOT use Pageable and individual sort/skip/limit simultaneously
        */
        if (false) {
            boolean usePageable = false;
            if (usePageable) {
                // # paging
                int pageNumber = 0; // zero-based page number, must not be negative.
                int pageSize = 5; // number of docs per a page to be returned, must be greater than 0.

                // # sorting : default is ASC
                // must not be null, use Sort.unsorted() instead.
                Sort sort = Sort.by(Sort.Direction.DESC, "_id");

                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                query.with(pageable);
            } else {
                // # sort
                //query.with(Sort.by(Sort.Direction.DESC, "_id"));

                // # skip
                query.skip(3);

                // # limit (max number of docs in response for this query)
                int limitSize = 5;
                query.limit(limitSize);
            }
        } else {
            int limitSize = 5;
            query.limit(limitSize);
        }

        // get query result
        List<Document> results = mongoTemplate.find(query, Document.class, COLLECTION_NAME);

        // Handle each document
        results.forEach(doc -> {
            System.out.println("----------------------------------------");
            System.out.println(JsonFormatter.prettyPrint(doc.toJson()));
            System.out.println(new ObjectId(doc.get("_id").toString()).getTimestamp());
        });
    }
}
