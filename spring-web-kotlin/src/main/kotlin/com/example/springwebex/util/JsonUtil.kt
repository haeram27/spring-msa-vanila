package com.example.springwebex.util

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper

object JsonUtil {
    
    private val JSON_MAPPER: JsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

    fun serialize(obj: Any?): String {
        if (obj == null) {
            throw IllegalArgumentException("obj must not be null.")
        }
        return JSON_MAPPER.writeValueAsString(obj)
    }

    fun <T> deserialize(jsonString: String?, clazz: Class<T>): T {
        if (StringUtil.isEmpty(jsonString)) {
            throw IllegalArgumentException("jsonString must not be null or empty.")
        }
        return deserialize(jsonString!!.toByteArray(), clazz)
    }

    fun <T> deserialize(jsonBytes: ByteArray?, clazz: Class<T>): T {
        if (CollectionUtil.isEmpty(jsonBytes)) {
            throw IllegalArgumentException("jsonBytes must not be null or empty.")
        }
        return JSON_MAPPER.readValue(jsonBytes, clazz)
    }

    fun <T> deserialize(jsonString: String?, ref: TypeReference<T>): T {
        if (StringUtil.isEmpty(jsonString)) {
            throw IllegalArgumentException("jsonString must not be null or empty.")
        }
        return deserialize(jsonString!!.toByteArray(), ref)
    }

    fun <T> deserialize(jsonBytes: ByteArray?, ref: TypeReference<T>): T {
        if (CollectionUtil.isEmpty(jsonBytes)) {
            throw IllegalArgumentException("jsonBytes must not be null or empty.")
        }
        return JSON_MAPPER.readValue(jsonBytes, ref)
    }
}
