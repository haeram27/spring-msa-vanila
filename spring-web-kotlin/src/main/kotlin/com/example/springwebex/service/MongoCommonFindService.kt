package com.example.springwebex.service

import com.example.springwebex.exception.RestApiException
import com.example.springwebex.exception.ecode.ErrorCode
import com.example.springwebex.model.restreq.MongoCommonFindReqDto
import com.example.springwebex.model.restresp.ResponseJsonDto
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import org.bson.Document
import org.bson.json.Converter
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.bson.json.StrictJsonWriter
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
class MongoCommonFindService(
    private val mongoTemplate: MongoTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = KotlinLogging.logger {}

    private val minPageNumber = 1
    private val minPageSize = 1
    private val maxPageSize = 1000

    private class ObjectIdConverter : Converter<ObjectId> {
        override fun convert(value: ObjectId, writer: StrictJsonWriter) {
            writer.writeString(value.toHexString())
        }
    }

    private class DateTimeConverter(private val zoneId: String) : Converter<Long> {
        override fun convert(value: Long, writer: StrictJsonWriter) {
            writer.writeString(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of(zoneId))
                )
            )
        }
    }

    fun find(request: MongoCommonFindReqDto): ResponseJsonDto<List<Map<String, Any>>> {
        val query = Query()

        val collection = if (StringUtils.hasText(request.collectionName)) request.collectionName else ""
        if (!StringUtils.hasText(collection)) {
            throw RestApiException(ErrorCode.BAD_REQUEST)
        }

        val tzId = if (StringUtils.hasText(request.timeZone)) request.timeZone else "UTC"
        val idStartDate = if (StringUtils.hasText(request.startDate)) request.startDate else ""
        val idEndDate = if (StringUtils.hasText(request.endDate)) request.endDate else ""

        if (StringUtils.hasText(idStartDate) && StringUtils.hasText(idEndDate)) {
            try {
                val fromLocalTime = LocalDateTime.parse(idStartDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val toLocalTime = LocalDateTime.parse(idEndDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val fromInstant = fromLocalTime.toInstant(ZoneId.of(tzId).rules.getOffset(fromLocalTime))
                val toInstant = toLocalTime.toInstant(ZoneId.of(tzId).rules.getOffset(toLocalTime))
                val fromId = ObjectId(Date.from(fromInstant))
                val toId = ObjectId(Date.from(toInstant))
                query.addCriteria(Criteria.where("_id").gte(fromId).lt(toId))
            } catch (e: DateTimeParseException) {
                throw RestApiException(ErrorCode.BAD_REQUEST)
            }
        }

        request.projectKeys?.let { keys ->
            if (keys.isNotEmpty()) {
                keys.forEach { key -> query.fields().include(key) }
            }
        }

        query.with(Sort.by(Sort.Order(Sort.Direction.DESC, "_id")))

        var pageNumber = request.pageNumber ?: 0
        var pageSize = request.pageSize ?: 0
        if (pageNumber < minPageNumber) pageNumber = minPageNumber
        if (pageSize !in minPageSize..maxPageSize) pageSize = maxPageSize

        val pageable = PageRequest.of(pageNumber - minPageNumber, pageSize)
        query.with(pageable)

        val results = mongoTemplate.find(query, Document::class.java, collection!!)
        log.debug { "nosql find($collection): results.size = ${results.size}" }

        val writerSettings = JsonWriterSettings.builder()
            .outputMode(JsonMode.RELAXED)
            .objectIdConverter(ObjectIdConverter())
            .dateTimeConverter(DateTimeConverter(tzId!!))
            .build()

        val mappedResults = mutableListOf<Map<String, Any>>()
        for (doc in results) {
            try {
                mappedResults.add(
                    objectMapper.readValue(doc.toJson(writerSettings), object : TypeReference<Map<String, Any>>() {})
                )
            } catch (ex: Exception) {
                throw RestApiException(ErrorCode.INTERNAL_SERVER_ERROR, ex)
            }
        }

        val response = ResponseJsonDto<List<Map<String, Any>>>()
        response.response = mappedResults
        return response
    }
}
