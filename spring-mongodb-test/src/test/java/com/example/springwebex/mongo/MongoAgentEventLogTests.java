package com.example.springwebex.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.DBObject;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
//@DataMongoTest
@Slf4j
public class MongoAgentEventLogTests {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void findTest() {
        Query query = new Query();

        // # match (criteria, document filtering)
        // criteria: value is
        // Map<String, Object> conditionMap = new HashMap<>();
        // conditionMap.put("node_id", 1);
        // conditionMap.put("ip", "1.1.1.1");
        // for (String key : conditionMap.keySet()) {
        //     query.addCriteria(Criteria.where(key).is(conditionMap.get(key)));
        // }

        // criteria: _id made in latest 24 hours
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Instant instant = twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant();
        ObjectId objectId = new ObjectId(Date.from(instant));
        query.addCriteria(Criteria.where("_id").gte(objectId));

        // # project (field filtering)
        List<String> fieldList = Arrays.asList("_id", "client_time", "node_id", "ip", "computer_name", "login_id",
                "department", "log_string_id", "log_string_args");
        for (String field : fieldList) {
            query.fields().include(field);
        }

        /* 
            !!! WARNING:
            Pageable redefines sort, skip, limit internally
            so do NOT use Pageable and individual sort/skip/limit simultaneously
        */
        boolean usePagination = false;
        if (usePagination) {
            // # paging
            int pageNumber = 0; // zero-based page number, must not be negative.
            int pageSize = 5; // number of docs per a page to be returned, must be greater than 0.
            Sort sort = Sort.by(Sort.Direction.DESC, "_id"); // must not be null, use Sort.unsorted() instead.

            Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
            query.with(pageable);
        } else {
            // # sort
            query.with(Sort.by(Sort.Direction.DESC, "_id"));

            // # skip
            query.skip(3);

            // # limit (max number of docs in response for this query)
            int limitSize = 3;
            query.limit(limitSize);
        }

        // get query result
        List<DBObject> results = mongoTemplate.find(query, DBObject.class, "tb_agent_event_log");

        // print result
        results.forEach(e -> {
            System.out.println("-----------------------------------");
            e.toMap().forEach((k, v) -> {
                System.out.println(k + " : " + v);
            });
        });
    }

    @Test
    public void aggregationTest() {

        // make criteria objectId with time before 24hours from current
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Instant instant = twentyFourHoursAgo.atZone(ZoneId.systemDefault()).toInstant();
        ObjectId objectId = new ObjectId(Date.from(instant));

        // limitSize (max number of docs in response for this query)
        int limitSize = 3;

        Aggregation agg = Aggregation.newAggregation(
                // match
                // Aggregation.match(Criteria.where("node_id").is(1)),
                // Aggregation.match(Criteria.where("ip").is("1.1.1.1")),
                Aggregation.match(Criteria.where("_id").gte(objectId)),

                // project
                Aggregation.project(new String[] { "_id", "client_time", "node_id", "ip", "computer_name", "login_id",
                        "department", "log_string_id", "log_string_args" }),

                // sort
                Aggregation.sort(Sort.Direction.DESC, "_id"),

                // limit
                Aggregation.limit(limitSize));

        // get aggregation result
        AggregationResults<DBObject> aggrResults = mongoTemplate.aggregate(agg, "tb_agent_event_log", DBObject.class);
        List<DBObject> results = aggrResults.getMappedResults();

        // print result
        results.forEach(e -> {
            System.out.println("-----------------------------------");
            e.toMap().forEach((k, v) -> {
                System.out.println(k + " : " + v);
            });
        });
    }
}
