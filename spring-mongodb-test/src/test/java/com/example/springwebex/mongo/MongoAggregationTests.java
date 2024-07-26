package com.example.springwebex.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.jayway.jsonpath.internal.JsonFormatter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest  // this annotation support only embedded mongo
@Slf4j
public class MongoAggregationTests {
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
     * default query result
     * order: ASC by ObjectId: _id
     */

    @Test
    public void aggregationTest() {

        // make criteria objectId with time before 24hours from current
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Instant instant = twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant();
        ObjectId objectId = new ObjectId(Date.from(instant));

        // limitSize (max number of docs in response for this query)
        int limitSize = 3;

        /* Definition of operation */
        // match
        MatchOperation matchOperationByNodeId = Aggregation.match(Criteria.where("node_id").is(1));
        MatchOperation matchOperationByIp = Aggregation.match(Criteria.where("ip").is("1.1.1.1"));
        MatchOperation matchOperationByObjectId = Aggregation.match(Criteria.where("_id").gte(objectId));
        // project
        ProjectionOperation projectOperation = Aggregation
                .project(new String[] { "_id" });
        // sort
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "_id");

        // limit
        LimitOperation limitOperation = Aggregation.limit(limitSize);

        Aggregation agg = Aggregation.newAggregation(
                // matchOperationByNodeId,
                // matchOperationByIp,
                matchOperationByObjectId,
                projectOperation,
                sortOperation,
                limitOperation);

        /*
            Get aggregation result
            # OutputType ::
            Userdefined Custom Class  ## Most Recommended for insert and update
            com.mongodb.DBObject(<3.2)  ## Most Recommended for query
        */
        String domainModelType = "Document";

        if ("Document".equals(domainModelType)) {
            AggregationResults<Document> aggrResults = mongoTemplate.aggregate(agg, "tb_agent_event_log",
                    Document.class);
            List<Document> results = aggrResults.getMappedResults();

            // Handle each document
            results.forEach(doc -> {
                System.out.println("----------------------------------------");
                System.out.println(JsonFormatter.prettyPrint(doc.toJson()));
            });
        } else {
            // AggregationResults<AgentEventLog> aggrResults = mongoTemplate.aggregate(agg, "tb_agent_event_log",
            //         AgentEventLog.class);
            // List<AgentEventLog> results = aggrResults.getMappedResults(); 
        }
    }
}
