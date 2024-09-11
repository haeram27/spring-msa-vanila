package com.example.springwebex.service;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.springwebex.exception.RestApiException;
import com.example.springwebex.model.restreq.MongoCommonFindReqDto;
import com.example.springwebex.model.restresp.ResponseJsonDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MongoCommonFindService {
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    private final int MIN_PAGE_NUMBER = 1;
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

    public ResponseJsonDto<List<Map<String, Object>>> find(MongoCommonFindReqDto request) {
        Query query = new Query();

        String collection = StringUtils.hasText(request.getCollectionName()) ? request.getCollectionName() : "";
        if (!StringUtils.hasText(collection)) {
            throw new RestApiException("Invalid collection name");
        }

        String tzId = StringUtils.hasText(request.getTimeZone()) ? request.getTimeZone() : "UTC";
        String idStartDate = StringUtils.hasText(request.getStartDate()) ? request.getStartDate() : "";
        String idEndDate = StringUtils.hasText(request.getEndDate()) ? request.getEndDate() : "";
        if (StringUtils.hasText(idStartDate) && StringUtils.hasText(idEndDate)) {
            try {
                var fromLocalTime = LocalDateTime.parse(idStartDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                var toLocalTime = LocalDateTime.parse(idEndDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                var fromInstant = fromLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(fromLocalTime));
                var toInstant = toLocalTime.toInstant(ZoneId.of(tzId).getRules().getOffset(toLocalTime));
                var fromId = new ObjectId(Date.from(fromInstant));
                var toId = new ObjectId(Date.from(toInstant));
                query.addCriteria(Criteria.where("_id").gte(fromId).lt(toId));
            } catch (DateTimeParseException e) {
                throw new RestApiException("Invalid datetime format");
            }
        }

        var projectKeys = Optional.ofNullable(request.getProjectKeys());
        if (projectKeys.isPresent() && projectKeys.get().size() > 0) {
            projectKeys.get().forEach((key) -> {
                query.fields().include(key);
            });
        }

        var orders = new ArrayList<Order>();
        orders.add(new Order(Direction.DESC, "_id"));
        var sort = Sort.by(orders);
        query.with(sort);

        int pageNumber = Optional.ofNullable(request.getPageNumber()).orElse(0);
        int pageSize = Optional.ofNullable(request.getPageSize()).orElse(0);

        if (pageNumber < MIN_PAGE_NUMBER)
            pageNumber = MIN_PAGE_NUMBER;
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE)
            pageSize = MAX_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber - MIN_PAGE_NUMBER, pageSize);
        query.with(pageable);

        List<Document> results = mongoTemplate.find(query, Document.class, collection);
        log.debug("nosql find({}): results.size = {}", collection, results.size());

        JsonWriterSettings writerSettings = JsonWriterSettings.builder()
                .outputMode(JsonMode.RELAXED)
                .objectIdConverter(new ObjectIdConverter())
                .dateTimeConverter(new DateTimeConverter(tzId))
                .build();

        var mappedResults = new ArrayList<Map<String, Object>>();
        for (var e : results) {
            try {
                mappedResults
                        .add(objectMapper.readValue(e.toJson(writerSettings), new TypeReference<Map<String, Object>>() {
                        }));
            } catch (JsonProcessingException ex) {
                throw new RestApiException(ex.getMessage());
            }
        }

        ResponseJsonDto<List<Map<String, Object>>> response = new ResponseJsonDto<>();
        response.setResponse(mappedResults);

        return response;
    }
}