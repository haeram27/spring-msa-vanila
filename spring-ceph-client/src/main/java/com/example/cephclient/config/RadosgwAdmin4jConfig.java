package com.example.cephclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

@Configuration
@EnableConfigurationProperties(RadosgwAdmin4jConfig.RadosgwAdminProperties.class)
public class RadosgwAdmin4jConfig {

    @Bean
    public RgwAdmin radosgwAdminClient(RadosgwAdminProperties properties) {
        return new RgwAdminBuilder()
            .accessKey(properties.getAccessKey())
            .secretKey(properties.getSecretKey())
            .endpoint(properties.getEndpoint())
            .build();
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "ceph.radosgw.admin")
    public static class RadosgwAdminProperties {
        private String endpoint = "http://127.0.0.1:80/admin";
        private String accessKey = "admin";
        private String secretKey = "admin";
        private String userId = "admin";
    }
}
