package com.example.springwebex.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.json.Converter;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictJsonWriter;
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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import com.example.springwebex.model.mongo.MongoSampleDoc;
import com.example.springwebex.model.mongo.finder.MongoCommonFindReq;
import com.example.springwebex.model.mongo.finder.MongoDateCriteria;
import com.example.springwebex.model.mongo.finder.MongoObjectIdCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class MongoCommonFindTests {
    private static final String MONGO_OBJECT_ID_KEY = "_id";

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

    private final String COLLECTION_NAME = "samples";

    private final int MIN_PAGE_NUMBER = 0;
    private final int MIN_PAGE_SIZE = 1;
    private final int MAX_PAGE_SIZE = 1000;

    private class ObjectIdConverter implements Converter<ObjectId> {
        @Override
        public void convert(ObjectId value, StrictJsonWriter writer) {
            writer.writeString(value.toHexString());
        }
    }

    private class DateTimeConverter implements Converter<Long> {
        private String zoneId;

        DateTimeConverter(String zoneId) {
            this.zoneId = zoneId;
        }

        @Override
        public void convert(Long value, StrictJsonWriter writer) {
            writer.writeString(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                            OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of(zoneId))));
        }
    }

    /*
     * find query and get docs as org.bson.Documen class
     */
    public List<Map<String, Object>> find(MongoCommonFindReq request, String collection) {
        Query query = new Query();

        String respTimezoneId = StringUtils.hasText(request.getRespTimezoneId()) ? request.getRespTimezoneId() : "UTC";

        if (request.getIdCriteria() != null) {
            var idCreteria = request.getIdCriteria();
            String tzId = StringUtils.hasText(idCreteria.getTimeZone()) ? idCreteria.getTimeZone() : "UTC";
            String idStartDate = StringUtils.hasText(idCreteria.getStartDate()) ? idCreteria.getStartDate() : "";
            String idEndDate = StringUtils.hasText(idCreteria.getEndDate()) ? idCreteria.getEndDate() : "";
            if (StringUtils.hasText(idStartDate) && StringUtils.hasText(idEndDate)) {
                try {
                    var fromLocalTime = LocalDateTime.parse(idStartDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    var toLocalTime = LocalDateTime.parse(idEndDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    var fromInstant = fromLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(fromLocalTime));
                    var toInstant = toLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(toLocalTime));
                    var fromId = new ObjectId(Date.from(fromInstant));
                    var toId = new ObjectId(Date.from(toInstant));
                    query.addCriteria(Criteria.where(MONGO_OBJECT_ID_KEY).gte(fromId).lt(toId));
                } catch (DateTimeParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (request.getDateCriterias() != null && !request.getDateCriterias().isEmpty()) {
            request.getDateCriterias().forEach(cri -> {
                String tzId = StringUtils.hasText(cri.getTimeZone()) ? cri.getTimeZone() : "UTC";
                String startDate = StringUtils.hasText(cri.getStartDate()) ? cri.getStartDate() : "";
                String endDate = StringUtils.hasText(cri.getEndDate()) ? cri.getEndDate() : "";
                if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
                    try {
                        var fromLocalTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        var toLocalTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        var fromInstant = fromLocalTime
                                .toInstant(ZoneId.of(tzId).getRules().getOffset(fromLocalTime));
                        var toInstant = toLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(toLocalTime));
                        var from = Date.from(fromInstant);
                        var to = Date.from(toInstant);
                        log.info("from : {}", from);

                        query.addCriteria(Criteria.where(cri.getKey()).gte(from).lt(to));
                    } catch (DateTimeParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        var projectKeys = Optional.ofNullable(request.getProjectKeys());
        if (projectKeys.isPresent() && projectKeys.get().size() > 0) {
            projectKeys.get().forEach((key) -> {
                query.fields().include(key);
            });
        }

        /* Sorting */
        var orders = new ArrayList<Order>();
        orders.add(new Order(Direction.DESC, "_id"));
        var sort = Sort.by(orders);
        query.with(sort);

        /* Pagenation */
        int pageNumber = Optional.ofNullable(request.getPageNumber()).orElse(0); // zero-based page number, must not be negative.
        int pageSize = Optional.ofNullable(request.getPageSize()).orElse(0); // number of docs per a page to be returned, must be greater than 0.

        if (pageNumber < MIN_PAGE_NUMBER)
            pageNumber = MIN_PAGE_NUMBER;
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE)
            pageSize = MAX_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        query.with(pageable);

        /* Get Results */
        List<Document> results = mongoTemplate.find(query, Document.class, collection);
// @formatter:off
/*
        results.forEach(doc -> {
            try {
                log.debug(JsonFormatter.prettyPrint(doc.toJson()));
            } catch (RuntimeException ex) {
                log.error(ex.getMessage());
            }
        });
*/

/*
        results.forEach(doc -> {
            try {
                log.debug(doc.toJson(JsonWriterSettings.builder()
                        .indent(true)
                        .build()));
            } catch (RuntimeException ex) {
                log.error(ex.getMessage());
            }
        });
*/

/*
        results.forEach(doc -> {
            try {
                // log.debug(doc.toJson(JsonWriterSettings.builder().indent(true).outputMode(JsonMode.SHELL).build()));
                // log.debug(doc.toJson(JsonWriterSettings.builder().indent(true).outputMode(JsonMode.EXTENDED).build()));
                log.debug(doc.toJson(JsonWriterSettings.builder().indent(true).build())); // JsonMode.RELAXED
            } catch (RuntimeException ex) {
                log.error(ex.getMessage());
            }
        });
*/
// @formatter:on
        log.debug("nosql find({}): results.size = {}", collection, results.size());

        /* adjust json value as favorite format */
        JsonWriterSettings writerSettings = JsonWriterSettings.builder()
                .outputMode(JsonMode.RELAXED)
                .objectIdConverter(new ObjectIdConverter())
                .dateTimeConverter(new DateTimeConverter(respTimezoneId))
                .build();

        var mappedResults = new ArrayList<Map<String, Object>>();
        for (var e : results) {
            try {
                mappedResults.add(objectMapper.readValue(e.toJson(writerSettings),
                        new TypeReference<Map<String, Object>>() {
                        }));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        return mappedResults;
    }

    /*
     * find query and get docs as designated model Object class
     * WARNING: This method can NOT support response timezone transforming
     */
    public <T> List<T> find(MongoCommonFindReq request, String collection, Class<T> clazz) {
        Query query = new Query();

        if (request.getIdCriteria() != null) {
            var idCreteria = request.getIdCriteria();
            String tzId = StringUtils.hasText(idCreteria.getTimeZone()) ? idCreteria.getTimeZone() : "UTC";
            String idStartDate = StringUtils.hasText(idCreteria.getStartDate()) ? idCreteria.getStartDate() : "";
            String idEndDate = StringUtils.hasText(idCreteria.getEndDate()) ? idCreteria.getEndDate() : "";
            if (StringUtils.hasText(idStartDate) && StringUtils.hasText(idEndDate)) {
                try {
                    var fromLocalTime = LocalDateTime.parse(idStartDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    var toLocalTime = LocalDateTime.parse(idEndDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    var fromInstant = fromLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(fromLocalTime));
                    var toInstant = toLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(toLocalTime));
                    var fromId = new ObjectId(Date.from(fromInstant));
                    var toId = new ObjectId(Date.from(toInstant));
                    query.addCriteria(Criteria.where(MONGO_OBJECT_ID_KEY).gte(fromId).lt(toId));
                } catch (DateTimeParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (request.getDateCriterias() != null && !request.getDateCriterias().isEmpty()) {
            request.getDateCriterias().forEach(cri -> {
                String tzId = StringUtils.hasText(cri.getTimeZone()) ? cri.getTimeZone() : "UTC";
                String startDate = StringUtils.hasText(cri.getStartDate()) ? cri.getStartDate() : "";
                String endDate = StringUtils.hasText(cri.getEndDate()) ? cri.getEndDate() : "";
                if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
                    try {
                        var fromLocalTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        var toLocalTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        var fromInstant = fromLocalTime
                                .toInstant(ZoneId.of(tzId).getRules().getOffset(fromLocalTime));
                        var toInstant = toLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(toLocalTime));
                        var from = Date.from(fromInstant);
                        var to = Date.from(toInstant);
                        query.addCriteria(Criteria.where(cri.getKey()).gte(from).lt(to));
                    } catch (DateTimeParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        var projectKeys = Optional.ofNullable(request.getProjectKeys());
        if (projectKeys.isPresent() && projectKeys.get().size() > 0) {
            projectKeys.get().forEach((key) -> {
                query.fields().include(key);
            });
        }

        /* Sorting */
        var orders = new ArrayList<Order>();
        orders.add(new Order(Direction.DESC, "_id"));
        var sort = Sort.by(orders);
        query.with(sort);

        /* Pagenation */
        int pageNumber = Optional.ofNullable(request.getPageNumber()).orElse(0); // zero-based page number, must not be negative.
        int pageSize = Optional.ofNullable(request.getPageSize()).orElse(0); // number of docs per a page to be returned, must be greater than 0.

        if (pageNumber < MIN_PAGE_NUMBER)
            pageNumber = MIN_PAGE_NUMBER;
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE)
            pageSize = MAX_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber - MIN_PAGE_NUMBER, pageSize);
        query.with(pageable);

        /* Get Results */
        List<T> results = mongoTemplate.find(query, clazz, collection);
        log.debug("nosql find({}): results.size = {}", collection, results.size());

        return results;
    }

    @Test
    public void findTest() {
        // define criteria
        var req = new MongoCommonFindReq();
        req.setRespTimezoneId("Asia/Seoul");

        req.setIdCriteria(MongoObjectIdCriteria.builder()
                .timeZone("Asia/Seoul")
                .startDate("2024-01-01T00:00:00")
                .endDate("2024-12-31T00:00:00")
                .build());

        var dateCriterias = new ArrayList<MongoDateCriteria>();
        dateCriterias.add(MongoDateCriteria.builder()
                .key("createdAt")
                .timeZone("Asia/Seoul")
                .startDate("2024-09-04T19:09:33.678")
                .endDate("2024-12-31T00:00:00.000")
                .build());
        req.setDateCriterias(dateCriterias);

        var projectKeys = new ArrayList<String>();
        projectKeys.add("_id");
        projectKeys.add("createdAt");
        req.setProjectKeys(projectKeys);

        req.setPageSize(100);
        req.setPageNumber(0);

        // query
        var results = find(req, COLLECTION_NAME);
        results.stream().forEach((map) -> {
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
        req.setRespTimezoneId("Asia/Seoul");

        req.setIdCriteria(MongoObjectIdCriteria.builder()
                .timeZone("Asia/Seoul")
                .startDate("2024-01-01T00:00:00")
                .endDate("2024-12-31T00:00:00")
                .build());

        var dateCriterias = new ArrayList<MongoDateCriteria>();
        dateCriterias.add(MongoDateCriteria.builder()
                .key("createdAt")
                .timeZone("Asia/Seoul")
                .startDate("2024-09-04T19:09:33.678")
                .endDate("2024-12-31T00:00:00.000")
                .build());
        req.setDateCriterias(dateCriterias);

        var projectKeys = new ArrayList<String>();
        projectKeys.add("_id");
        projectKeys.add("createdAt");
        req.setProjectKeys(projectKeys);

        req.setPageSize(100);
        req.setPageNumber(0);

        // query
        var results = find(req, COLLECTION_NAME, MongoSampleDoc.class);
        results.stream().forEach((obj) -> {
            System.out.println(obj);
        });
    }

}
