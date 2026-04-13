package com.example.springsecex.util;

import java.io.IOException;
import java.util.Objects;

import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public final class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // prevent UnknownPropertyException
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private JsonUtil() {
        // do nothing
    }

    public static String serialize(Object obj) throws JacksonException {
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
