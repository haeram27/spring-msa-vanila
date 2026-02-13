package com.example.springwebex.util.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.springwebex.util.mongo.model.MongoCommonFindReq;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoFindUtils {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // prevent UnknownPropertyException
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private static final String MONGO_OBJECT_ID_KEY = "_id";
    private static final String TIMEZONE_ID_UTC = "UTC";

    private static final int MIN_PAGE_NUMBER = 0;
    private static final int MIN_PAGE_SIZE = 1;
    private static final int DEFAULT_PAGE_SIZE = 100000;


    public static class ObjectIdConverter implements Converter<ObjectId> {
        @Override
        public void convert(ObjectId value, StrictJsonWriter writer) {
            writer.writeString(value.toHexString());
        }
    }


    public static class DateTimeConverter implements Converter<Long> {
        private String zoneId;

        public DateTimeConverter(String zoneId) {
            this.zoneId = zoneId;
        }

        @Override
        public void convert(Long value, StrictJsonWriter writer) {
            writer.writeString(
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                        OffsetDateTime.ofInstant(Instant.ofEpochMilli(value),
                        StringUtils.hasText(zoneId) ? ZoneId.of(zoneId) : ZoneOffset.UTC)));
        }
    }


    /**
     *  transform Document to Map<String, Object> using ObjectMapper
        example: 
            List<Document> results = MongoFindUtils.find(mongoTemplate, req);
            List<Map<String, Object>> mappedResults = MongoFindUtils.mapDocuments(results, null,
                    new MongoFindUtils.ObjectIdConverter(),
                    new MongoFindUtils.DateTimeConverter(req.getRespTimezoneId()));
     * @param docs
     * @param mode
     * @param idConverter
     * @param dateConverter
     * @return
     */

    public static List<Map<String, Object>> mapDocuments(List<Document> docs, JsonMode mode,
            ObjectIdConverter idConverter, DateTimeConverter dateConverter) {
        /* adjust json value as favorite format */
        var builder = JsonWriterSettings.builder();

        if (mode != null)
            builder.outputMode(mode);
        else
            builder.outputMode(JsonMode.RELAXED);

        if (idConverter != null)
            builder.objectIdConverter(idConverter);

        if (dateConverter != null)
            builder.dateTimeConverter(dateConverter);

        var mappedResults = new ArrayList<Map<String, Object>>();
        for (var e : docs) {
            try {
                mappedResults.add(objectMapper.readValue(e.toJson(builder.build()),
                    new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        return mappedResults;
    }


    private static Query assembleFindQuery(MongoCommonFindReq request) throws RuntimeException {
        Query query = new Query();

        if (request.getIdCriteria() != null) {
            var idCreteria = request.getIdCriteria();
            String tzId = StringUtils.hasText(idCreteria.getTimezoneId()) ? idCreteria.getTimezoneId()
                    : TIMEZONE_ID_UTC;
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
                String tzId = StringUtils.hasText(cri.getTimezoneId()) ? cri.getTimezoneId() : TIMEZONE_ID_UTC;
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

        var equalCriterias = request.getEqualCriterias();
        if (!CollectionUtils.isEmpty(equalCriterias)) {
            equalCriterias.forEach((k, v) -> {
                query.addCriteria(Criteria.where(k).is(v));
            });
        }

        var inCriterias = request.getEqualCriterias();
        if (!CollectionUtils.isEmpty(inCriterias)) {
            inCriterias.forEach((k, v) -> {
                if (v instanceof List)
                    query.addCriteria(Criteria.where(k).in(v));
            });
        }

        var regexCriterias = request.getRegexCriterias();
        if (!CollectionUtils.isEmpty(regexCriterias)) {
            regexCriterias.forEach((k, v) -> {
                query.addCriteria(Criteria.where(k).regex(v));
            });
        }

        /* Sorting */
        var orders = new ArrayList<Order>();
        var reqOrders = request.getOrders();
        if (CollectionUtils.isEmpty(reqOrders)) {
            orders.add(new Order(Direction.DESC, MONGO_OBJECT_ID_KEY));
        } else {
            reqOrders.forEach((k, v) -> {
                if ("asc".equalsIgnoreCase((String) v)) {
                    orders.add(new Order(Direction.ASC, k));
                } else if ("desc".equalsIgnoreCase((String) v)) {
                    orders.add(new Order(Direction.DESC, k));
                };
            });
        }
        query.with(Sort.by(orders));

        /* Pagenation */
        int pageNumber = Optional.ofNullable(request.getPageNumber()).orElse(0); // zero-based page number, must not be negative.
        int pageSize = Optional.ofNullable(request.getPageSize()).orElse(0); // number of docs per a page to be returned, must be greater than 0.

        if (pageNumber < MIN_PAGE_NUMBER) pageNumber = MIN_PAGE_NUMBER;
        if (pageSize < MIN_PAGE_SIZE) pageSize = DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber - MIN_PAGE_NUMBER, pageSize);
        return query.with(pageable);
    }


    /*
     * find query and get docs as org.bson.Documen class
     */
    public static List<Document> find(MongoTemplate mongoTemplate, MongoCommonFindReq request) throws RuntimeException {
        var collection = StringUtils.hasText(request.getCollection()) ? request.getCollection() : "";
        if (!StringUtils.hasText(collection)) {
            throw new RuntimeException("invalid collection name");
        }

        /* Get Results */
        var results = mongoTemplate.find(
                assembleFindQuery(request)
                , Document.class
                , collection);
        log.debug("nosql find({}): results.size = {}", collection, results.size());

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

        /* adjust json value as favorite format */
        
        return results;
    }


    /*
     * find query and get docs as designated model Object class
     * WARNING: This method can NOT support response timezone transforming
     */
    public static <T> List<T> find(MongoTemplate mongoTemplate, MongoCommonFindReq request, Class<T> clazz) throws RuntimeException {
        var collection = StringUtils.hasText(request.getCollection()) ? request.getCollection() : "";
        if (!StringUtils.hasText(collection)) {
            throw new RuntimeException("invalid collection name");
        }

        /* Get Results */
        List<T> results = mongoTemplate.find(assembleFindQuery(request), clazz, collection);
        log.debug("nosql find({}): results.size = {}", collection, results.size());

        return results;
    }
}
