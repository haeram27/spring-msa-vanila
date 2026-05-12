package com.example.cephclient.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.EvaluatedTimeTests;
import com.example.cephclient.s3.S3PresignerFacade;
import com.example.cephclient.s3.S3ClientFacade;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@SpringBootTest
class S3PresignerFacadeIntegrationTests extends EvaluatedTimeTests {

    @Autowired
    private S3PresignerFacade facade;

    @Autowired
    private S3ClientFacade s3ClientFacade;

    @Autowired
    private S3Presigner s3Presigner;

    @Test
    void presignPutObject_WithRealCeph_ShouldReturnSignedUrl() {
        String bucketName = createTestBucket();
        String key = "integration/put-" + Instant.now().toEpochMilli() + ".txt";

        S3PresignerFacade.PresignResult result = facade.presignPutObject(
            new S3PresignerFacade.PutObjectUrlRequest(
                bucketName,
                key,
                "text/plain",
                11L,
                120
            )
        );

        log.info("[presignPutObject] method={}, url={}, expiresAt={}", result.method(), result.url(), result.expiresAt());

        assertThat(result.method()).isEqualTo("PUT");
        assertThat(result.url()).isNotBlank();
        assertThat(result.url()).contains("X-Amz-Algorithm=");
    }

    @Test
    void s3PresignerBean_WithRealCeph_ShouldGeneratePresignedPutRequest() {
        String bucketName = createTestBucket();
        String key = "integration/direct-put-" + Instant.now().toEpochMilli() + ".txt";

        var presignedRequest = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(2))
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("text/plain")
                        .build()
                )
                .build()
        );

        log.info(
            "[s3Presigner.presignPutObject] method={}, url={}, expiresAt={}",
            presignedRequest.httpRequest().method().name(),
            presignedRequest.url(),
            presignedRequest.expiration()
        );

        assertThat(presignedRequest.url().toString()).isNotBlank();
        assertThat(presignedRequest.httpRequest().method().name()).isEqualTo("PUT");
    }

    @Test
    void presignHeadBucket_WithExistingBucket_ShouldReturnSignedUrl() {
        String bucketName = createTestBucket();

        S3PresignerFacade.PresignResult result = facade.presignHeadBucket(
            new S3PresignerFacade.HeadBucketUrlRequest(bucketName, 120)
        );

        log.info(
            "[presignHeadBucket] method={}, url={}, expiresAt={}",
            result.method(),
            result.url(),
            result.expiresAt()
        );

        assertThat(result.method()).isEqualTo("HEAD");
        assertThat(result.url()).isNotBlank();
        assertThat(result.url()).contains("X-Amz-Algorithm=");
    }

    private String createTestBucket() {
        String bucketName = "it-presigner-" + Instant.now().toEpochMilli();
        boolean created = s3ClientFacade.createBucket(bucketName);

        log.info("[createBucket] bucketName={}, created={}", bucketName, created);

        assertThat(created).isTrue();
        return bucketName;
    }
}
