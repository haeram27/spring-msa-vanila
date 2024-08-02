package com.example.springwebex.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.example.springwebex.util.mongo.ObjectIdUtils;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest  // this annotation support only embedded mongo
@Slf4j
public class MongoAggregationSampleDocTests {
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

    @Test
    public void aggregationMatchTest() {

        /* Definition of operation */
        // match
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        ObjectId fromObjectId = ObjectIdUtils
                .getObjectIdFromDate(LocalDateTime.parse("2024-08-01T00:00:00", formatter), ZoneOffset.UTC);
        ObjectId toObjectId = ObjectIdUtils
                .getObjectIdFromDate(LocalDateTime.parse("2024-08-02T00:00:00", formatter), ZoneOffset.UTC);

        MatchOperation matchOperationByObjectId = Aggregation
                .match(Criteria.where("_id").gte(fromObjectId).lt(toObjectId));

        // project
        ProjectionOperation projectOperation = Aggregation
                .project(new String[] { "_id", "createdAt" });
        // sort
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "_id", "createdAt");

        // # pagenation : skip + limit
        int pageSize = 5; // number of docs in a page
        int pageNumber = 0; // page number beggins from zero

        // skip
        SkipOperation skipOperation = Aggregation.skip(pageSize * pageNumber);

        // limit
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation agg = Aggregation.newAggregation(
                matchOperationByObjectId,
                projectOperation,
                sortOperation,
                skipOperation,
                limitOperation);

        /*
            Get aggregation result
            # OutputType ::
            Userdefined Custom Class  ## Most Recommended for insert and update
            com.mongodb.DBObject(<3.2)  ## Most Recommended for query
        */
        AggregationResults<Document> aggrResults = mongoTemplate.aggregate(agg, COLLECTION_NAME,
                Document.class);
        List<Document> results = aggrResults.getMappedResults();

        // Handle each document
        results.forEach(doc -> {
            System.out.println("----------------------------------------");
            System.out.println(JsonFormatter.prettyPrint(doc.toJson()));
        });
        System.out.println("Size of Results: " + results.size());
    }

    /*  
     * default query result
     * order: ASC by ObjectId: _id
     */

    @Test
    public void aggregationGroupTest() {

        // make criteria objectId with time before 24hours from current
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Instant instant = twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant();
        ObjectId objectId = new ObjectId(Date.from(instant));

        /* Definition of operation */
        MatchOperation matchOperation = Aggregation
                .match(Criteria.where("booleanField").exists(true).and("groupField").exists(true));
        // group
        GroupOperation groupOperation = Aggregation.group("booleanField", "groupField").count().as("count");

        /* ---- START of output format ---- */
        // sort
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "_id");

        /*
         * PAGENATION
         * For aggregation, pagenation SHOULD accomplished with skip and limit operation
         */

        // # pagenation : skip + limit
        int pageNumber = 0; // zero-based page number, must not be negative.
        int pageSize = 5; // number of docs in a page to be returned, must be greater than 0.

        // skip
        SkipOperation skipOperation = Aggregation.skip(pageSize * pageNumber);

        // limit
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation agg = Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                sortOperation,
                skipOperation,
                limitOperation);

        /*
            Get aggregation result
            # OutputType ::
            Userdefined Custom Class  ## Most Recommended for insert and update
            com.mongodb.DBObject(<3.2)  ## Most Recommended for query
        */
        AggregationResults<Document> aggrResults = mongoTemplate.aggregate(agg, COLLECTION_NAME,
                Document.class);
        List<Document> results = aggrResults.getMappedResults();

        // Handle each document
        results.forEach(doc -> {
            System.out.println("----------------------------------------");
            System.out.println(JsonFormatter.prettyPrint(doc.toJson()));
        });
        System.out.println("Size of Results: " + results.size());
    }
}
