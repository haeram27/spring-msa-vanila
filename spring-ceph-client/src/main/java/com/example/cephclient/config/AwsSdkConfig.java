package com.example.cephclient.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(AwsSdkConfig.AwsS3Properties.class)
public class AwsSdkConfig {

    @Bean
    public S3Client cephS3Client(AwsS3Properties properties) {
        var builder = S3Client.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                )
            )
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(properties.isPathStyleAccessEnabled())
                    .build()
            );

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner cephS3Presigner(AwsS3Properties properties) {
        var builder = S3Presigner.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                )
            )
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(properties.isPathStyleAccessEnabled())
                    .build()
            );

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "ceph.aws.s3")
    public static class AwsS3Properties {
        private String endpoint = "http://127.0.0.1:7480";
        private String region = "us-east-1";
        private String accessKey = "admin";
        private String secretKey = "admin";
        private boolean pathStyleAccessEnabled = true;
    }
}
