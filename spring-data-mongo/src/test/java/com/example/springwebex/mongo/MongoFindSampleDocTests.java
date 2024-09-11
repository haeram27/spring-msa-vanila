package com.example.springwebex.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

import com.example.springwebex.model.mongo.MongoSampleDoc;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest  // this annotation support only embedded mongo
@Slf4j
public class MongoFindSampleDocTests {
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

    /*
     *  # find() query configuration and apply order
     *  criteria (match, sql.where)
     *  project (sql.select)
     * ---- inquery condition END ----
     * ---- output format START ----
     *  sort (default: ASC by $oid, recomm: DESC by $oid)
     *  skip (default: none)
     *  limit (default: none)
     */

    @SuppressWarnings("unused")
    @Test
    public void findTest() {
        Query query = new Query();

        // # match (criteria, document filtering)
        // criteria: $oid in 24 hours
        if (false) {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            Instant instant = twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant();
            ObjectId objectId = new ObjectId(Date.from(instant));
            query.addCriteria(Criteria.where("_id").gt(objectId));
        }

        // criteria: DATETIME Range criteria by ObjectID($oid)
        //           ObjectID's time data has precision(resolution) of "second" !!!
        if (false) {
            // DATE/TIME Range
            var zid = "Asia/Seoul";
            var fromLocalTime = LocalDateTime.parse("2024-07-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            var toLocalTime = LocalDateTime.parse("2024-08-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            var fromInstant = fromLocalTime.toInstant(ZoneId.of(zid).getRules().getOffset(fromLocalTime));
            var toInstant = toLocalTime.toInstant(ZoneId.of(zid).getRules().getOffset(toLocalTime));
            var fromId = new ObjectId(Date.from(fromInstant));
            var toId = new ObjectId(Date.from(toInstant));
            // ObjectId.isValid(fromId.toHexString());
            // ObjectId.isValid(toId.toHexString());
            query.addCriteria(Criteria.where("_id").gte(fromId).lt(toId));
        }

        // criteria: ISODate has precision(resolution) of "milli-second"
        if (true) {
            int dateCompareWay = 2;
            if (dateCompareWay == 0) { // recommended
                Date fromDate = Date.from(Instant.parse("2024-08-02T00:38:29.616Z"));
                Date toDate = Date.from(Instant.parse("2024-08-02T00:38:29.619Z"));
                query.addCriteria(Criteria.where("dateField").gte(fromDate).lt(toDate));
            } else if (dateCompareWay == 1) { // NOT recommended
                Date fromDate = Date
                        .from(LocalDateTime.parse("2024-08-01T04:50:09.511", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                .atZone(ZoneOffset.UTC).toInstant());
                Date toDate = Date
                        .from(LocalDateTime.parse("2024-08-01T04:50:09.513", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                .atZone(ZoneOffset.UTC).toInstant());

                query.addCriteria(Criteria.where("dateField").gte(fromDate).lt(toDate));
            } else if (dateCompareWay == 2) {
                Date targetDate = Date.from(Instant.parse("2024-08-01T04:50:09.512Z"));
                query.addCriteria(Criteria.where("dateField").is(targetDate));
            }
        }

        // criteria: value condition
        if (false) {
            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put("booleanField", true);
            for (String key : conditionMap.keySet()) {
                query.addCriteria(Criteria.where(key).is(conditionMap.get(key)));
            }
        }

        // criteria regex pattern
        // https://www.mongodb.com/docs/manual/reference/operator/query/regex
        if (false) {
            query.addCriteria(Criteria.where("stringField").regex("^Sam.*\\d{4}$"));
        }

        /* # project (field filtering)
         * include vs exclude ?
         * include and exclude field can not be configured simutaneously
         * only last include or exclude definition is stored in query
         */
        String projectType = "INCLUDE"; // NONE, INCLUDE, EXCLUDE
        if ("INCLUDE".equals(projectType)) {
            // # project: include
            List<String> includeFieldList = Arrays.asList("_id", "dateField");
            // List<String> includeFieldList = Arrays.asList("_id", "objectIdField", "booleanField");
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
         * PAGENATION
         * pagenation can be accomplished skip and limit with mongoTemplate
         * when using mongoTemplate pagenation of skip and limit way,
         * it is recommended to use criteria of date/time interval through ObjectId or ISODate Object.
         * 
         * If it is needed cursor way for pagenation, low layer api SHOULD be used with collection.find()   
         */

        // PAGENATION, LIMIT, NONE
        String pagenation = "LIMIT";
        if ("PAGENATION".equals(pagenation)) {
            /*
                PageNation
                !!! WARNING:
                Query.with(Pageable) sets sort, skip, limit in Query internally
                so do NOT use Pageable and individual sort/skip/limit simultaneously
            */

            int pageNumber = 0; // zero-based page number, must not be negative.
            int pageSize = 5; // number of docs per a page to be returned, must be greater than 0.

            boolean usePageable = false;
            if (usePageable) {
                // # sorting : default is ASC
                // must not be null, use Sort.unsorted() instead.
                Sort sort = Sort.by(Sort.Direction.DESC, "_id");

                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                query.with(pageable);
            } else {
                // # sort
                query.with(Sort.by(Sort.Direction.DESC, "_id"));

                // # skip
                query.skip(pageNumber * pageSize);
                query.limit(pageSize);
            }
        } else if ("LIMIT".equals(pagenation)) {
            // # limit (max number of docs in response for this query)
            int limitSize = 5;
            query.limit(limitSize);
        }

        // get query result
        boolean useDto = true;
        if (useDto) {
            List<MongoSampleDoc> results = mongoTemplate.find(query, MongoSampleDoc.class, COLLECTION_NAME);

            // Handle each document
            results.forEach(doc -> {
                System.out.println("----------------------------------------");
                System.out.println(doc.getId().getDate().toInstant().atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH-mm-ssX")));

                System.out.println(doc.getDateField().toInstant().atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH-mm-ss.SSSX")));
            });

            System.out.println("Size of Results: " + results.size());
        } else {
            List<Document> results = mongoTemplate.find(query, Document.class, COLLECTION_NAME);

            // Handle each document
            results.forEach(doc -> {
                System.out.println("----------------------------------------");
                System.out.println(JsonFormatter.prettyPrint(doc.toJson()));
                System.out.println(doc.getObjectId("_id").getDate().toInstant().atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH-mm-ssX")));
                System.out.println(doc.get("dateField"));
                // System.out.println(doc.get("_id"));
            });

            System.out.println("Size of Results: " + results.size());
        }
    }
}
