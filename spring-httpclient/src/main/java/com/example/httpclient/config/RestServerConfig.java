package com.example.httpclient.config;

import java.nio.file.Files;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.httpclient.model.RestServerConfigDto;
import com.example.httpclient.util.PathUtil;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestServerConfig {

    private final YAMLMapper yamlMapper;

    /*
     * read rest server information to request from "rest-server.yaml"
     */
    @Bean
    public RestServerConfigDto RestServerInfo() {
        var path = PathUtil.processWorkingDirectory().resolve(
            "rest-server.yaml"
        ).normalize();

        log.info(path.toString());

        if (!(Files.exists(path) && Files.isRegularFile(path))) {
            log.error("can NOT find configuration file");
        }

        RestServerConfigDto yaml = new RestServerConfigDto();
        try {
            yaml = yamlMapper.readValue(path.toFile(), RestServerConfigDto.class);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

        return yaml;
    }
}
