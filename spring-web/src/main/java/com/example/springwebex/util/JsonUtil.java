package com.example.springwebex.util;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // prevent UnknownPropertyException
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private JsonUtil() {
        // do nothing
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        if (Objects.isNull(obj)) {
            throw new IllegalArgumentException("obj must not be null.");
        }

        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public static <T> T deserialize(String jsonString, Class<T> clazz)
            throws IOException, StreamReadException, DatabindException {
        if (StringUtil.isEmpty(jsonString)) {
            throw new IllegalArgumentException("jsonString must not be null or empty.");
        }

        return deserialize(jsonString.getBytes(), clazz);
    }

    public static <T> T deserialize(byte[] jsonBytes, Class<T> clazz)
            throws IOException, StreamReadException, DatabindException {
        if (CollectionUtil.isEmpty(jsonBytes)) {
            throw new IllegalArgumentException("jsonBytes must not be null or empty.");
        }
        return OBJECT_MAPPER.readValue(jsonBytes, clazz);
    }

    public static <T> T deserialize(String jsonString, TypeReference<T> ref)
            throws IOException, StreamReadException, DatabindException {
        if (StringUtil.isEmpty(jsonString)) {
            throw new IllegalArgumentException("jsonString must not be null or empty.");
        }

        return deserialize(jsonString.getBytes(), ref);
    }

    public static <T> T deserialize(byte[] jsonBytes, TypeReference<T> ref)
            throws IOException, StreamReadException, DatabindException {
        if (CollectionUtil.isEmpty(jsonBytes)) {
            throw new IllegalArgumentException("jsonBytes must not be null or empty.");
        }

        return OBJECT_MAPPER.readValue(jsonBytes, ref);
    }
}
