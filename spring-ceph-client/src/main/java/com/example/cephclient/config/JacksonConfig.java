package com.example.cephclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public JsonMapper restClientJsonMapper() {

        return JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // prevent UnknownPropertyException
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }

    @Bean
    public YAMLMapper restClientYamlMapper() {

        return YAMLMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // prevent UnknownPropertyException
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
}
