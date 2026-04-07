package com.example.springwebex.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object JsonUtil {
    
    private val OBJECT_MAPPER: ObjectMapper = JsonMapper.builder()
        .addModule(JavaTimeModule())
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

    fun serialize(obj: Any?): String {
        if (obj == null) {
            throw IllegalArgumentException("obj must not be null.")
        }
        return OBJECT_MAPPER.writeValueAsString(obj)
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
        return OBJECT_MAPPER.readValue(jsonBytes, clazz)
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
        return OBJECT_MAPPER.readValue(jsonBytes, ref)
    }
}
